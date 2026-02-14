# POD Platform

## Quick Start

### 1. Database Init
Run SQL scripts in `/db`:
- `init_demo_data.sql`
- `init_demo_data_wms_tms.sql`
- `init_demo_data_ai.sql` (New)
- Apply patches in `/db/patch/` if needed.

### 2. XXL-JOB Setup
1. Download and start `xxl-job-admin` (v2.4.0) at port 8088.
2. Login `admin/123456`.
3. Create Executor: `pod-os-executor` (Auto registration).
4. Create Jobs:
   - `helloJob` (Cron: `0 * * * * ?`)
   - `aiDiagnoseWorker` (Cron: `0/10 * * * * ?`, Sharding Broadcast)
   - `renderTaskWorker`
   - `renderRetryJobHandler`
   - `integrationPullOrdersMock`
   - `tmsAckRetry`

### 3. AI Demo
**Create Diagnose Task**
```bash
curl -X POST http://localhost:8080/api/ai/diagnose \
  -H "Content-Type: application/json" \
  -H "X-Request-Id: req_001" \
  -H "X-Tenant-Id: 1001" \
  -H "X-Factory-Id: 2001" \
  -d '{"bizType":"ORDER", "bizNo":"OB001"}'
```

**Run Job**
Trigger `aiDiagnoseWorker` in XXL-JOB Admin.

**Check Result**
```bash
curl http://localhost:8080/api/ai/tasks/AI_OB001
```
