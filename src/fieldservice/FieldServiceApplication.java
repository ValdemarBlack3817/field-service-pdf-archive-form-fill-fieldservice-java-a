package fieldservice;

public final class FieldServiceApplication {
    public static void main(String[] args) throws Exception {
        FieldServiceConfig config = FieldServiceConfig.fromEnvironment();
        WorkOrderPdfService service = new WorkOrderPdfService(new InfraiHttpClient(config));
        WorkOrderPdfService.WorkOrder order = new WorkOrderPdfService.WorkOrder(
                "WO-1042", "DISPATCHED", "photo/front-door.jpg", "photo/meter.jpg", "Recheck seal on Friday");
        String key = service.fillAndArchive(order, config.pdfBase64());
        System.out.println("ARCHIVED " + key);
    }
}
