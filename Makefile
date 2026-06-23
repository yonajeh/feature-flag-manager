JAVA_HOME ?= $(shell /usr/libexec/java_home -v 21 2>/dev/null)
export JAVA_HOME
export PATH := $(JAVA_HOME)/bin:$(PATH)

.PHONY: help test backend-test frontend-test verify docker-build compose-up compose-down

help:
	@echo "Targets: test, backend-test, frontend-test, verify, docker-build, compose-up, compose-down"

backend-test:
	cd apps/backend && mvn clean verify

frontend-test:
	cd apps/frontend && npm ci && npm test -- --watch=false --browsers=ChromeHeadless

test: backend-test frontend-test

verify: backend-test

docker-build:
	docker build -f docker/Dockerfile -t feature-flag-manager:latest .

compose-up:
	docker compose -f docker/docker-compose.yml up --build -d

compose-down:
	docker compose -f docker/docker-compose.yml down -v
