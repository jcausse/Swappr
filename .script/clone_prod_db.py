#!/usr/bin/env python3

"""
Clone the production database into a new local Docker container running postgres:latest.
Pulls credentials from deploy_secrets.properties, dumps the production database via SSH,
transfers the dump locally, spins up a fresh container on a randomized port and name,
restores the database, and cleans up temporary dump files.
"""

import argparse
import os
import random
import shutil
import socket
import subprocess
import sys
import time
import uuid
from configparser import RawConfigParser, NoSectionError, NoOptionError
from datetime import datetime
from subprocess import run, CalledProcessError
from typing import Dict, Optional

DEFAULT_SECRETS_FILENAME = './.script/deploy_secrets.properties'
SECRETS_SECTION = 'DEPLOYMENT'
SECRETS_OPTIONS = [
    'ssh_username',
    'ssh_password',
    'ssh_server',
    'ssh_port',
]

DATABASE_SECTION = 'DATABASE'
DATABASE_OPTIONS = [
    'db_host',
    'db_user',
    'db_password',
    'db_database'
]

TARGET_DB_NAME = 'paw-prod-clone'
TARGET_USER = 'postgres'
TARGET_PASSWORD = 'postgres'
TARGET_IMAGE = 'postgres:latest'


def check_prerequisites() -> None:
    """
    Ensure all necessary command-line tools are available.
    """
    tools = ['docker', 'ssh', 'scp', 'sshpass']
    missing = [tool for tool in tools if shutil.which(tool) is None]
    if missing:
        print(f"Error: Required tool(s) not found in PATH: {', '.join(missing)}")
        print("Please ensure docker, openssh-client, and sshpass are installed.")
        sys.exit(1)

    try:
        run(['docker', 'info'], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    except (CalledProcessError, FileNotFoundError):
        print("Error: Docker daemon is not running or accessible. Please start Docker.")
        sys.exit(1)


def find_secrets_file(specified_path: Optional[str] = None) -> str:
    """
    Locate the deploy_secrets.properties file.
    """
    if specified_path:
        if not os.path.exists(specified_path):
            raise FileNotFoundError(f"Specified secrets file not found: {specified_path}")
        return specified_path

    candidates = [
        DEFAULT_SECRETS_FILENAME,
        os.path.join(os.path.dirname(os.path.abspath(__file__)), 'deploy_secrets.properties'),
        'deploy_secrets.properties',
    ]

    for path in candidates:
        if os.path.exists(path):
            return path

    return candidates[0]


def load_secrets(filename: str) -> Dict[str, str]:
    """
    Load SSH and database credentials from secrets file.
    """
    print("Loading secrets...", end=' ', flush=True)
    if not os.path.exists(filename):
        print(f"Secrets file not found at '{filename}'.")
        raise FileNotFoundError(f"Secrets file not found at '{filename}'.")

    config = RawConfigParser()
    config.read(filename)

    secrets = {}

    try:
        section = SECRETS_SECTION
        for option in SECRETS_OPTIONS:
            secrets[option] = config.get(section, option)
        section = DATABASE_SECTION
        for option in DATABASE_OPTIONS:
            secrets[option] = config.get(section, option)
    except NoSectionError:
        print(f"Section '{section}' not found in '{filename}'.")
        raise
    except NoOptionError:
        print(f"Option '{option}' of section '{section}' not found in '{filename}'.")
        raise

    print("Done.")
    return secrets


def find_available_port(min_port: int = 15432, max_port: int = 60000) -> int:
    """
    Find a random available port on the host to avoid port collisions.
    """
    candidates = list(range(min_port, max_port))
    random.shuffle(candidates)
    for port in candidates:
        available = True
        for host in ['127.0.0.1', '0.0.0.0']:
            try:
                with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
                    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
                    s.bind((host, port))
            except OSError:
                available = False
                break
        if available:
            return port
    raise RuntimeError("Could not find an available random port on the host.")


def generate_container_name() -> str:
    """
    Generate a random, collision-free container name.
    """
    while True:
        suffix = uuid.uuid4().hex[:8]
        name = f"paw-prod-clone-{suffix}"
        res = run(
            ['docker', 'inspect', name],
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL
        )
        if res.returncode != 0:
            return name


def dump_remote_db(secrets: Dict[str, str], dump_filename: str) -> None:
    """
    Generate a dump of the remote production database using pg_dump via SSH.
    """
    print("Dumping production database on remote server...", end=' ', flush=True)

    ssh_username = secrets['ssh_username']
    ssh_password = secrets['ssh_password']
    ssh_server = secrets['ssh_server']
    ssh_port = secrets['ssh_port']

    db_host = secrets['db_host']
    db_user = secrets['db_user']
    db_password = secrets['db_password']
    db_database = secrets['db_database']

    escaped_password = db_password.replace("'", "'\\''")
    dump_command = (
        f"PGPASSWORD='{escaped_password}' pg_dump -h {db_host} -U {db_user} "
        f"-d {db_database} -F c -b -f {dump_filename}"
    )

    ssh_command = [
        'sshpass', '-p', ssh_password,
        'ssh',
        '-p', str(ssh_port),
        '-o', 'StrictHostKeyChecking=no',
        f"{ssh_username}@{ssh_server}",
        dump_command
    ]

    try:
        proc = run(ssh_command, capture_output=True, text=True, check=True)
        print("Done.")
    except CalledProcessError as e:
        print("Failed.")
        print(f"Error executing remote dump:\n{e.stderr or e.stdout}")
        raise


def download_dump(secrets: Dict[str, str], dump_filename: str, local_destination_path: str) -> None:
    """
    Download the dump file from the remote home directory to the local destination via SCP.
    """
    print("Downloading database dump via SCP...", end=' ', flush=True)

    ssh_username = secrets['ssh_username']
    ssh_password = secrets['ssh_password']
    ssh_server = secrets['ssh_server']
    ssh_port = secrets['ssh_port']

    remote_file_path = f"/home/{ssh_username}/{dump_filename}"

    scp_command = [
        'sshpass', '-p', ssh_password,
        'scp',
        '-P', str(ssh_port),
        '-o', 'StrictHostKeyChecking=no',
        f"{ssh_username}@{ssh_server}:{remote_file_path}",
        local_destination_path
    ]

    try:
        run(scp_command, capture_output=True, text=True, check=True)
        print("Done.")
    except CalledProcessError as e:
        print("Failed.")
        print(f"Error downloading dump file via SCP:\n{e.stderr or e.stdout}")
        raise


def remove_remote_dump(secrets: Dict[str, str], dump_filename: str) -> None:
    """
    Remove the temporary dump file on the remote server.
    """
    print("Cleaning up remote dump file on server...", end=' ', flush=True)

    ssh_username = secrets['ssh_username']
    ssh_password = secrets['ssh_password']
    ssh_server = secrets['ssh_server']
    ssh_port = secrets['ssh_port']

    remote_file_path = f"/home/{ssh_username}/{dump_filename}"
    ssh_command = [
        'sshpass', '-p', ssh_password,
        'ssh',
        '-p', str(ssh_port),
        '-o', 'StrictHostKeyChecking=no',
        f"{ssh_username}@{ssh_server}",
        f"rm -f {remote_file_path}"
    ]

    try:
        run(ssh_command, capture_output=True, text=True, check=True)
        print("Done.")
    except Exception as e:
        print(f"Warning: Failed to remove remote dump file: {e}")


def start_docker_container(container_name: str, port: int) -> None:
    """
    Spin up a new Docker container with postgres:latest.
    """
    print(f"Starting Docker container '{container_name}' on port {port} using image '{TARGET_IMAGE}'...", end=' ', flush=True)

    docker_run_command = [
        'docker', 'run',
        '--name', container_name,
        '-e', f'POSTGRES_USER={TARGET_USER}',
        '-e', f'POSTGRES_PASSWORD={TARGET_PASSWORD}',
        '-e', f'POSTGRES_DB={TARGET_DB_NAME}',
        '-p', f"{port}:5432",
        '-d',
        TARGET_IMAGE
    ]

    try:
        run(docker_run_command, capture_output=True, text=True, check=True)
        print("Done.")
    except CalledProcessError as e:
        print("Failed.")
        print(f"Error starting Docker container:\n{e.stderr or e.stdout}")
        raise


def wait_for_postgres(container_name: str, timeout: int = 60) -> None:
    """
    Wait until PostgreSQL in the container is fully initialized and accepting queries.
    """
    print("Waiting for PostgreSQL inside container to be ready...", end=' ', flush=True)
    start_time = time.time()

    check_cmd = [
        'docker', 'exec', container_name,
        'psql', '-U', TARGET_USER, '-d', TARGET_DB_NAME, '-c', 'SELECT 1;'
    ]

    while time.time() - start_time < timeout:
        res = run(check_cmd, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        if res.returncode == 0:
            print("Ready.")
            return
        time.sleep(1)

    print("Timed out.")
    raise TimeoutError(f"PostgreSQL container '{container_name}' did not become ready within {timeout} seconds.")


def restore_database(container_name: str, local_dump_path: str, dump_filename: str) -> None:
    """
    Copy the dump file into the container and restore it with pg_restore.
    """
    container_dump_path = f"/tmp/{dump_filename}"

    print("Copying dump into container...", end=' ', flush=True)
    try:
        run(['docker', 'cp', local_dump_path, f"{container_name}:{container_dump_path}"], check=True, capture_output=True)
        print("Done.")
    except CalledProcessError as e:
        print("Failed.")
        print(f"Error copying dump into container:\n{e.stderr}")
        raise

    print("Restoring database with pg_restore...", end=' ', flush=True)
    restore_command = [
        'docker', 'exec', container_name,
        'pg_restore',
        '-U', TARGET_USER,
        '-d', TARGET_DB_NAME,
        '--no-owner',
        '--no-acl',
        container_dump_path
    ]

    try:
        proc = run(restore_command, capture_output=True, text=True)
        # pg_restore returns 0 on success, 1 on non-fatal warnings (e.g. public schema exists), >1 on error
        if proc.returncode > 1:
            print("Failed.")
            print(f"Error restoring database:\n{proc.stderr}")
            raise CalledProcessError(proc.returncode, restore_command, proc.stdout, proc.stderr)
        print("Done.")
    finally:
        run(['docker', 'exec', container_name, 'rm', '-f', container_dump_path], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

    # Verify table count
    try:
        verify_proc = run(
            ['docker', 'exec', container_name, 'psql', '-U', TARGET_USER, '-d', TARGET_DB_NAME, '-t', '-A', '-c',
             "SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public';"],
            capture_output=True,
            text=True,
            check=True
        )
        table_count = verify_proc.stdout.strip()
        print(f"Database populated successfully: {table_count} tables restored in schema 'public'.")
    except Exception as e:
        print(f"Warning: Could not verify restored tables: {e}")


def print_success_summary(container_name: str, port: int) -> None:
    """
    Print the exact credentials, database name, port, and container name.
    """
    banner = "=" * 70
    print()
    print(banner)
    print("  PRODUCTION DATABASE CLONED SUCCESSFULLY")
    print(banner)
    print(f"  Container Name : {container_name}")
    print(f"  Database Name  : {TARGET_DB_NAME}")
    print(f"  Username       : {TARGET_USER}")
    print(f"  Password       : {TARGET_PASSWORD}")
    print(f"  Host           : localhost (127.0.0.1)")
    print(f"  Port           : {port}")
    print(f"  Docker Image   : {TARGET_IMAGE}")
    print(banner)
    print("  Connection Details:")
    print(f"    JDBC URL     : jdbc:postgresql://localhost:{port}/{TARGET_DB_NAME}")
    print(f"    psql CLI     : docker exec -it {container_name} psql -U {TARGET_USER} -d {TARGET_DB_NAME}")
    print(f"    psql (host)  : PGPASSWORD='{TARGET_PASSWORD}' psql -h localhost -p {port} -U {TARGET_USER} -d {TARGET_DB_NAME}")
    print()
    print("  Container Management:")
    print(f"    Stop container   : docker stop {container_name}")
    print(f"    Start container  : docker start {container_name}")
    print(f"    Remove container : docker rm -f {container_name}")
    print(banner)
    print()


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Clone the production database into a new local Docker container running postgres:latest."
    )
    parser.add_argument(
        "--secrets",
        default=None,
        help="Path to deploy_secrets.properties file (default: auto-detected in ./.script/)"
    )
    parser.add_argument(
        "--port",
        type=int,
        default=None,
        help="Port to expose on host (default: random available port)"
    )
    parser.add_argument(
        "--container-name",
        default=None,
        help="Container name (default: randomized paw-prod-clone-XXXXXXXX)"
    )
    parser.add_argument(
        "--keep-dump",
        action="store_true",
        help="Do not delete the local and remote database dump files after restoration"
    )
    args = parser.parse_args()

    check_prerequisites()

    secrets_path = find_secrets_file(args.secrets)
    secrets = load_secrets(secrets_path)

    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    dump_filename = f"db_clone_{timestamp}.dump"
    local_dump_path = os.path.abspath(dump_filename)

    container_name = args.container_name or generate_container_name()
    port = args.port or find_available_port()

    container_started = False
    remote_dump_created = False
    local_dump_downloaded = False

    try:
        # Step 1: Dump remote database
        dump_remote_db(secrets, dump_filename)
        remote_dump_created = True

        # Step 2: Download dump locally to current working directory
        download_dump(secrets, dump_filename, local_dump_path)
        local_dump_downloaded = True

        # Step 3: Remove remote dump file from server unless requested to keep
        if not args.keep_dump:
            remove_remote_dump(secrets, dump_filename)
            remote_dump_created = False

        # Step 4: Start local Docker container with postgres:latest
        start_docker_container(container_name, port)
        container_started = True

        # Step 5: Wait for Postgres to be ready
        wait_for_postgres(container_name)

        # Step 6: Restore database
        restore_database(container_name, local_dump_path, dump_filename)

        # Step 7: Print output credentials and details
        print_success_summary(container_name, port)

    except KeyboardInterrupt:
        print("\n\nOperation cancelled by user.")
        if container_started:
            print(f"Cleaning up container '{container_name}'...", end=' ', flush=True)
            run(['docker', 'rm', '-f', container_name], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            print("Done.")
        sys.exit(130)

    except Exception as e:
        print(f"\n=== CLONE FAILED ===")
        print(f"Error: {e}")
        if container_started:
            print(f"Removing failed container '{container_name}'...", end=' ', flush=True)
            run(['docker', 'rm', '-f', container_name], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            print("Done.")
        sys.exit(1)

    finally:
        # Step 8: Clean up local dump file unless requested to keep
        if local_dump_downloaded and not args.keep_dump and os.path.exists(local_dump_path):
            print("Cleaning up local dump file...", end=' ', flush=True)
            try:
                os.remove(local_dump_path)
                print("Done.")
            except Exception as e:
                print(f"Warning: Failed to delete local dump file: {e}")

        # Clean up remote dump file if still lingering
        if remote_dump_created and not args.keep_dump:
            try:
                remove_remote_dump(secrets, dump_filename)
            except Exception:
                pass


if __name__ == '__main__':
    main()
