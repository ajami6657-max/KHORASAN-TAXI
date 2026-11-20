# Zaffran Taxi Backend

FastAPI + PostgreSQL foundation for the Zaffran Taxi platform.

## Local development
1. Install Python 3.11+ and PostgreSQL 15+.
2. Create a virtual environment.
3. Install dependencies: `pip install -r requirements.txt`.
4. Start the API: `uvicorn app.main:app --reload`.

## Current scope
- Health and finance-capability status endpoints.
- PostgreSQL migration for driver payout preferences, double-entry ledger, payout requests and daily payout batches.
- Money is stored as integer IRR, never floating point.

## Not production-ready yet
No payment or bank transfer is executed. AP credentials, official API integration, signed callback verification, ledger posting service, database migrations runner, authentication/authorization, KYC/IBAN verification, payout scheduler, reconciliation and security tests must be implemented and reviewed before real-money use. Never commit API keys, passwords, private keys or bank data.
