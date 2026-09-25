package fungsi;

public class NotifikasiModel {
    private final long id;
    private final String eventType;
    private final String title;
    private final String body;
    private final String sourceTable;
    private final String sourcePk;
    private final String createdAt;

    public NotifikasiModel(long id, String eventType, String title, String body, String sourceTable, String sourcePk, String createdAt) {
        this.id = id;
        this.eventType = eventType;
        this.title = title;
        this.body = body;
        this.sourceTable = sourceTable;
        this.sourcePk = sourcePk;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public String getEventType() { return eventType; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getSourceTable() { return sourceTable; }
    public String getSourcePk() { return sourcePk; }
    public String getCreatedAt() { return createdAt; }
}
