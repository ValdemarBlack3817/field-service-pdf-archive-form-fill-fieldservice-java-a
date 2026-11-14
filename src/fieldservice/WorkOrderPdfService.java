package fieldservice;

public final class WorkOrderPdfService {
    public record WorkOrder(String id, String dispatchStatus, String photoOne, String photoTwo, String followUp) {}
    private final InfraiHttpClient client;

    public WorkOrderPdfService(InfraiHttpClient client) { this.client = client; }

    public boolean eligible(WorkOrder order) {
        return order.dispatchStatus().equals("DISPATCHED") && !order.followUp().isBlank();
    }

    public String fillAndArchive(WorkOrder order, String pdfBase64) throws Exception {
        if (!eligible(order)) throw new IllegalArgumentException("work order is not ready for technician follow-up");
        String fields = "{\"work_order_id\":\"" + order.id() + "\",\"dispatch_status\":\"" + order.dispatchStatus()
                + "\",\"photo_1\":\"" + order.photoOne() + "\",\"photo_2\":\"" + order.photoTwo()
                + "\",\"technician_follow_up\":\"" + order.followUp() + "\"}";
        String filled = client.fill(pdfBase64, fields, "fill-" + order.id());
        String key = order.id() + "/flattened.pdf";
        client.archive(key, InfraiHttpClient.dataBase64(filled), "archive-" + order.id());
        return key;
    }
}
