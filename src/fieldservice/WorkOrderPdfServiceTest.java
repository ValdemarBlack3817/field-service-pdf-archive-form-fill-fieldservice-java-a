package fieldservice;

public final class WorkOrderPdfServiceTest {
    public static void main(String[] args) {
        WorkOrderPdfService service = new WorkOrderPdfService(null);
        var ready = new WorkOrderPdfService.WorkOrder("WO-1", "DISPATCHED", "a", "b", "Call customer");
        var waiting = new WorkOrderPdfService.WorkOrder("WO-2", "UNASSIGNED", "a", "b", "Call customer");
        if (!service.eligible(ready) || service.eligible(waiting)) throw new AssertionError("eligibility decision changed");
        System.out.println("PASS: dispatched follow-up orders are eligible; unassigned orders are held");
    }
}
