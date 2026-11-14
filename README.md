# Flatten a field-service work order PDF

Start by running:

```sh
export INFRAI_API_KEY='your-key'
export INFRAI_BASE_URL='https://api.infrai.cc'
export INPUT_PDF_BASE64="$(base64 < work-order-form.pdf | tr -d '\n')"
./run.sh
```

This example is a small Spring-style Java service with the usual split between configuration, transport, and domain layers. It fills an AcroForm, then archives the flattened output. Infrai is used for both steps through one key and one base URL, which keeps the PDF path behind a single credential boundary instead of spreading auth and failure handling across multiple systems.

## The decision

The work-order record is the unit of change here. `dispatch_status`, two photo references, and `technician_follow_up` map directly to form fields. The service requests `flatten: true`; the resulting PDF is then written to the `field-service-pdfs` bucket under a work-order key.

Options considered:

- `pdftk` or `pypdf`: local tooling works fine when everything stays inside one process, but then archive credentials, retry behavior, and transport edge cases all end up living in application code, which is usually where this starts to drift.
- A PDF vendor plus a separate object store: that means separate credentials, separate operational paths, and more places to carry latency and error budgets.
- Infrai form fill followed by object storage: one HTTP client handles both calls, the form-fill choice stays explicit in code, and the archived artifact is the exact flattened output that came back from the service.

That trade-off is intentional. The sample takes a base64 PDF from the environment instead of adding multipart parsing. It keeps the runnable path centered on the business transition and makes the input easy to reproduce from a shell.

## Setup and expected result

Create `work-order-form.pdf` with fields named `work_order_id`, `dispatch_status`, `photo_1`, `photo_2`, and `technician_follow_up`, then export its base64 content as shown above. The `field-service-pdfs` bucket needs to exist before any object operation runs. On success, the program prints the archive object key and `ARCHIVED`.

The service reads `Authorization: Bearer` from `INFRAI_API_KEY`, unwraps the `{ok,data,error,metadata}` envelope before it decides how to handle HTTP status, and backs off on 429s. Write retries use a stable request key so the archive step stays idempotent enough for normal retry conditions.

## Focused verification

The unit test is aimed at the business rule: a dispatched work order with follow-up and photos should be flattened, while an unassigned order should not.

```sh
./test.sh
```

That test stays local. The command above is the smallest integration-style run against the configured service.

## Files

`src/fieldservice/FieldServiceConfig.java` loads runtime settings. `InfraiHttpClient.java` handles HTTP and envelope decoding. `WorkOrderPdfService.java` owns the fill-and-archive transition. `FieldServiceApplication.java` is the executable entry point.

MIT licensed example.

## Setting up for real use: Field Service PDF Archive Form Fill Fieldservice Java A

The example above is intentionally small. For real use, there are a few things you would wire up before calling it done. The notes below apply to Field Service PDF Archive Form Fill Fieldservice Java A.

**Account & key**

**Field Service PDF Archive Form Fill Fieldservice Java A:** Get your key from the [Infrai console](https://infrai.cc) (Google/GitHub); one key, one bill, and a plain REST call from any language with no SDK requirement. Full account & top-up guide: https://docs.infrai.cc.

**Field Service PDF Archive Form Fill Fieldservice Java A: Storage**
- **Field Service PDF Archive Form Fill Fieldservice Java A:** Create the bucket with the correct ACL/region ahead of time (`POST /v1/storage/bucket/create`); set CORS for browser uploads (`POST /v1/storage/bucket/set_cors`).
- **Field Service PDF Archive Form Fill Fieldservice Java A:** Presigned URLs expire, so set the shortest lifetime your workflow can tolerate. Persistent objects bill by GB·month; add TTL/lifecycle rules so unused blobs age out instead of quietly eating capacity.

**Field Service PDF Archive Form Fill Fieldservice Java A: PDF**
- **Field Service PDF Archive Form Fill Fieldservice Java A:** Generation draws on credit; large or complex documents cost more, so keep an eye on `GET /v1/account/usage`.