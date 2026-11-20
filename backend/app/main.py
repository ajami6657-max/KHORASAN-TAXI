from fastapi import FastAPI

app = FastAPI(
    title="Zaffran Taxi API",
    version="0.1.0",
    description="Backend foundation. Financial operations remain disabled until provider and bank contracts are verified.",
)

@app.get("/health", tags=["system"])
async def health():
    return {"status": "ok", "service": "zaffran-api"}

@app.get("/v1/finance/status", tags=["finance"])
async def finance_status():
    return {
        "ledger_schema": "planned_for_migration_001",
        "ap_gateway": "not_connected",
        "instant_payout": "disabled_pending_provider_approval",
        "daily_payout": "disabled_pending_bank_and_scheduler_setup",
        "real_money_operations_enabled": False,
    }
