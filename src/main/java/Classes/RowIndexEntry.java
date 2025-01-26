package Classes;

public class RowIndexEntry {
    private final String columnValue;
    private long numericValue;
    private final int lineNumber;

    public RowIndexEntry(String columnValue, int lineNumber) {
        this.columnValue = columnValue;
        this.lineNumber = lineNumber;
    }

    public void setNumeric(boolean isNumeric) {
        if (isNumeric) {
            try {
                this.numericValue = Long.parseLong(columnValue);
            } catch (NumberFormatException e) {
                this.numericValue = Long.MAX_VALUE;
            }
        }
    }

    public String getColumnValue() {
        return columnValue;
    }

    public long getNumericValue() {
        return numericValue;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
