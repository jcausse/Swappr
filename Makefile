MAVEN  := mvn
PYTHON := python3

.PHONY: dev dev-clean clean pack compile deploy db-start db-stop troubleshoot import-secrets

########################################################################################################################
# LOCAL DEVELOPMENT TARGETS

dev: db-start
	$(MAVEN) install -DskipTests -Pdev
	$(MAVEN) -pl webapp jetty:run -Pdev

dev-clean: clean dev

recompile: clean
	$(MAVEN) compile

clean:
	$(MAVEN) clean

########################################################################################################################
# LOCAL DEVELOPMENT DATABASE TARGETS

db-start:
	@./.script/db-start.sh

db-stop:
	@./.script/db-stop.sh

########################################################################################################################
# TESTING AND TROUBLESHOOTING TARGETS

troubleshoot:
	@$(PYTHON) ./.script/troubleshooter/main.py $(ARGS)

test:
	$(MAVEN) clean test

########################################################################################################################
# PRODUCTION TARGETS

prod-deploy:
	@$(PYTHON) ./.script/deploy.py

prod-db-backup:
	@$(PYTHON) ./.script/deploy.py --db-backup-only

########################################################################################################################
# MISCELLANEOUS TARGETS

import-secrets:
	@./.script/import-secrets.sh
