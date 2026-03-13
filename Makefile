# ============================================================
#  ufind-backend — environment management
# ============================================================

include .env.dev
export


DEV_COMPOSE  = docker compose -f docker-compose.dev.yml
PROD_COMPOSE = docker compose -f docker-compose.prod.yml

AWSLOCAL = AWS_ACCESS_KEY_ID=$(RUSTFS_ACCESS_KEY) \
           AWS_SECRET_ACCESS_KEY=$(RUSTFS_SECRET_KEY) \
           AWS_DEFAULT_REGION=us-east-1 \
           aws --endpoint-url=http://localhost:9000

.DEFAULT_GOAL := help

# ── help ─────────────────────────────────────────────────────
.PHONY: help
help:
	@echo ""
	@echo "  ufind-backend — available commands"
	@echo ""
	@echo "  Dev"
	@echo "    make dev          — build and start all dev services (foreground)"
	@echo "    make dev-up       — build and start all dev services (detached)"
	@echo "    make dev-down     — stop and remove dev containers"
	@echo "    make dev-rebuild  — force full rebuild of the dev app image"
	@echo "    make dev-logs     — tail logs for all dev services"
	@echo "    make dev-ps       — list running dev containers"
	@echo ""
	@echo "  Prod"
	@echo "    make prod         — build and start all prod services (detached)"
	@echo "    make prod-down    — stop and remove prod containers"
	@echo "    make prod-rebuild — force full rebuild of the prod app image"
	@echo "    make prod-logs    — tail logs for all prod services"
	@echo "    make prod-ps      — list running prod containers"
	@echo ""
	@echo "  Database"
	@echo "    make dev-psql     — open psql shell in the dev db container"
	@echo "    make prod-psql    — open psql shell in the prod db container"
	@echo ""
	@echo "  Storage (RustFS)"
	@echo "    make s3-buckets   — list buckets in RustFS"
	@echo "    make s3-files     — list files in the ufind-dev bucket"
	@echo "    make s3-reset     — destroy and recreate RustFS containers"
	@echo "    make s3-console   — print RustFS console URL"
	@echo ""
	@echo "  Cleanup"
	@echo "    make clean-dev    — remove dev containers, volumes, and orphans"
	@echo "    make clean-prod   — remove prod containers, volumes, and orphans"
	@echo "    make clean-all    — remove everything above + unused Docker images"
	@echo ""

# ── dev ──────────────────────────────────────────────────────
.PHONY: dev
dev:
	$(DEV_COMPOSE) up --build

.PHONY: dev-up
dev-up:
	$(DEV_COMPOSE) up --build -d

.PHONY: dev-down
dev-down:
	$(DEV_COMPOSE) down

.PHONY: dev-rebuild
dev-rebuild:
	$(DEV_COMPOSE) build --no-cache app
	$(DEV_COMPOSE) up -d

.PHONY: dev-logs
dev-logs:
	$(DEV_COMPOSE) logs -f

.PHONY: dev-ps
dev-ps:
	$(DEV_COMPOSE) ps

# ── prod ─────────────────────────────────────────────────────
.PHONY: prod
prod:
	$(PROD_COMPOSE) up --build -d

.PHONY: prod-down
prod-down:
	$(PROD_COMPOSE) down

.PHONY: prod-rebuild
prod-rebuild:
	$(PROD_COMPOSE) build --no-cache app
	$(PROD_COMPOSE) up -d

.PHONY: prod-logs
prod-logs:
	$(PROD_COMPOSE) logs -f

.PHONY: prod-ps
prod-ps:
	$(PROD_COMPOSE) ps

# ── database ─────────────────────────────────────────────────
.PHONY: dev-psql
dev-psql:
	$(DEV_COMPOSE) exec db psql -U dev -d devdb

.PHONY: prod-psql
prod-psql:
	$(PROD_COMPOSE) exec db psql -U $${POSTGRES_USER} -d $${POSTGRES_DB}

# ── storage (rustfs) ─────────────────────────────────────────
.PHONY: s3-buckets
s3-buckets:
	$(AWSLOCAL) s3 ls

.PHONY: s3-files
s3-files:
	$(AWSLOCAL) s3 ls s3://ufind-dev

.PHONY: s3-reset
s3-reset:
	$(DEV_COMPOSE) rm -sf rustfs rustfs-init
	$(DEV_COMPOSE) up -d rustfs rustfs-init

.PHONY: s3-console
s3-console:
	@echo "RustFS console: http://localhost:9001"
	@echo "  user:     test"
	@echo "  password: testtest123"

# ── cleanup ──────────────────────────────────────────────────
.PHONY: clean-dev
clean-dev:
	$(DEV_COMPOSE) down -v --remove-orphans

.PHONY: clean-prod
clean-prod:
	$(PROD_COMPOSE) down -v --remove-orphans

.PHONY: clean-all
clean-all: clean-dev clean-prod
	docker image prune -f