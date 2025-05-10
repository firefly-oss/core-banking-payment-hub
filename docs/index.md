# Firefly Core Banking Payment Hub Documentation

## Overview

Welcome to the comprehensive documentation for the Firefly Core Banking Payment Hub. This documentation provides detailed information about the payment types supported by the platform, technical implementation details, and integration guides.

## Documentation Structure

The documentation is organized into the following sections:

### Payment Types

Detailed documentation for each payment type supported by the Firefly Core Banking Payment Hub:

- [SEPA Payments](payment-types/sepa-payments.md) - Single Euro Payments Area payments
- [SWIFT Payments](payment-types/swift-payments.md) - International payments using SWIFT network
- [ACH Payments](payment-types/ach-payments.md) - US Automated Clearing House payments
- [UK Payments](payment-types/uk-payments.md) - UK payment systems (FPS, BACS, CHAPS)
- [European Payments](payment-types/european-payments.md) - TARGET2, TIPS, and EBA STEP2 payments
- [Internal Transfers](payment-types/internal-transfers.md) - Transfers between accounts within the core banking system

### Technical Documentation

- [Architecture Overview](technical/architecture.md) - Detailed explanation of the hexagonal architecture
- [Provider Registry](technical/provider-registry.md) - How the provider registry works
- [Payment Provider Standardization](technical/payment-provider-standardization.md) - Standardized approach to payment providers
- [Data Models](technical/data-models.md) - Explanation of the data models used in the system
- [SCA Implementation](technical/sca-implementation.md) - Strong Customer Authentication implementation details
- [Biometric Authentication](technical/biometric-authentication.md) - Biometric authentication capabilities
- [Payment Cancellation](technical/cancellation.md) - Payment cancellation flow and implementation
- [Health Monitoring](technical/health-monitoring.md) - System health monitoring capabilities