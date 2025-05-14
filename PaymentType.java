public enum PaymentType {
    CARD("card"),
    CASH("cash"),
    BONUS_POINT("bonuspoint");

    private final String dbValue;
    PaymentType(String dbValue){ this.dbValue = dbValue; }
    public String getDbValue(){ return dbValue; }

    public static PaymentType fromDbValue(String val){
        for(PaymentType pt: values())
            if(pt.dbValue.equalsIgnoreCase(val)) return pt;
        throw new IllegalArgumentException("Unknown payment type: "+val);
    }
}