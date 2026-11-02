.DEFAULT_GOAL := dev

MAVEN  := mvn
PYTHON := python3

.PHONY: dev dev-clean clean pack compile deploy db-start db-stop troubleshoot import-secrets help

########################################################################################################################
# LOCAL DEVELOPMENT TARGETS

dev: db-start ## Start development server and database
	$(MAVEN) install -DskipTests -Pdev
	$(MAVEN) -pl webapp jetty:run -Pdev

dev-clean: clean dev ## Clean pre-compiled sources, then start development server and database

recompile: clean ## Clean pre-compiled sources and recompile
	$(MAVEN) compile

clean: ## Perform a cleanup
	$(MAVEN) clean

########################################################################################################################
# LOCAL DEVELOPMENT DATABASE TARGETS

db-start: ## Start local development database (Docker)
	@./.script/db-start.sh

db-stop: ## Stop local development database (Docker)
	@./.script/db-stop.sh

########################################################################################################################
# TESTING AND TROUBLESHOOTING TARGETS

troubleshoot: ## Run Python troubleshooters (see .script/troubleshooter)
	@$(PYTHON) ./.script/troubleshooter/main.py $(ARGS)

test: ## Run all Java tests
	$(MAVEN) clean test

########################################################################################################################
# PRODUCTION TARGETS

prod-deploy: ## Back-up production database, then build and deploy from working directory (requires .script/deploy_secrets.properties)
	@$(PYTHON) ./.script/deploy.py

prod-db-backup: ## Back-up production database via SSH (requires .script/deploy_secrets.properties)
	@$(PYTHON) ./.script/deploy.py --db-backup-only

########################################################################################################################
# MISCELLANEOUS TARGETS

import-secrets: ## Automatically import secrets (properties files and rememberMeKey) to their respective locations (requires secrets.tar.gz)
	@./.script/import-secrets.sh

help: ## Show this help message
	@echo "Usage: make [target]"
	@echo ""
	@echo "Targets:"
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z0-9_-]+:.*?## / {printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)
