---
layout: default
---
## Overview

Cougar is a modular system, discovering modules at runtime based on predefined rules to search in your classpath.

Modules are defined by the existence of a Spring config file `conf/cougar-<module-type>-spring.xml`.

Modules types may be singletons.

Precedence/loading order:
1. Bootstrap modules
2. Core module
3. Framework modules
4. Application modules

## Bootstrap Modules

*Module Type*: `bootstrap`
*Singleton*: NO

## Core Module

*Module Type*: `core`
*Singleton*: YES

## Framework Modules

*Module Type*: `module`
*Singleton*: NO

## Application Modules

*Module Type*: `application`
*Singleton*: NO