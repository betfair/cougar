---
layout: default
---
## Overview

Cougar is a modular system, discovering modules at runtime based on predefined rules to search in your classpath.

Modules are defined by the existence of a Spring config file `conf/cougar-<module-type>-spring.xml`.

Modules types may be singletons.

Precedence/loading order:
# Bootstrap modules
# Core module
# Framework modules
# Application modules

## Bootstrap Modules

**Module Type**: `bootstrap`
**Singleton**: (x)

## Core Module

**Module Type**: `core`
**Singleton**: (/)

## Framework Modules

**Module Type**: `module`
**Singleton**: (x)

## Application Modules

**Module Type**: `application`
**Singleton**: (x)