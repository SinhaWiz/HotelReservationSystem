package com.hotel.model;

/**
 * Model class representing an invoice line item
 */
public class InvoiceLineItem {
    
    public enum ItemType {
        ROOM, SERVICE, TAX, DISCOUNT, EXTRA_CHARGE
    }
    
    private long lineItemId;
    private long invoiceId;
    private ItemType itemType;
    private String itemDescription;
    private double quantity;
    private double unitPrice;
    private double lineTotal;
    private Integer serviceId;
    private Long usageId;
    
    // Related objects
    private Invoice invoice;
    private RoomService roomService;
    private ServiceUsage serviceUsage;
    
    // Constructors
    public InvoiceLineItem() {
        this.quantity = 1.0;
    }
    
    public InvoiceLineItem(long invoiceId, ItemType itemType, String itemDescription,
                          double quantity, double unitPrice) {
        this();
        this.invoiceId = invoiceId;
        this.itemType = itemType;
        this.itemDescription = itemDescription;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = quantity * unitPrice;
    }
    
    public InvoiceLineItem(long invoiceId, ItemType itemType, String itemDescription,
                          double unitPrice) {
        this(invoiceId, itemType, itemDescription, 1.0, unitPrice);
    }
    
    // Getters and Setters
    public long getLineItemId() {
        return lineItemId;
    }
    
    public void setLineItemId(long lineItemId) {
        this.lineItemId = lineItemId;
    }
    
    public long getInvoiceId() {
        return invoiceId;
    }
    
    public void setInvoiceId(long invoiceId) {
        this.invoiceId = invoiceId;
    }
    
    public ItemType getItemType() {
        return itemType;
    }
    
    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }
    
    public String getItemTypeString() {
        return itemType != null ? itemType.name() : "";
    }
    
    public void setItemTypeFromString(String itemTypeString) {
        try {
            this.itemType = ItemType.valueOf(itemTypeString);
        } catch (IllegalArgumentException e) {
            this.itemType = ItemType.SERVICE;
        }
    }
    
    public String getItemDescription() {
        return itemDescription;
    }
    
    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }
    
    public double getQuantity() {
        return quantity;
    }
    
    public void setQuantity(double quantity) {
        this.quantity = quantity;
        // Recalculate line total
        this.lineTotal = quantity * unitPrice;
    }
    
    public double getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        // Recalculate line total
        this.lineTotal = quantity * unitPrice;
    }
    
    public double getLineTotal() {
        return lineTotal;
    }
    
    public void setLineTotal(double lineTotal) {
        this.lineTotal = lineTotal;
    }
    
    public Integer getServiceId() {
        return serviceId;
    }
    
    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }
    
    public Long getUsageId() {
        return usageId;
    }
    
    public void setUsageId(Long usageId) {
        this.usageId = usageId;
    }
    
    public Invoice getInvoice() {
        return invoice;
    }
    
    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
        if (invoice != null) {
            this.invoiceId = invoice.getInvoiceId();
        }
    }
    
    public RoomService getRoomService() {
        return roomService;
    }
    
    public void setRoomService(RoomService roomService) {
        this.roomService = roomService;
        if (roomService != null) {
            this.serviceId = roomService.getServiceId();
        }
    }
    
    public ServiceUsage getServiceUsage() {
        return serviceUsage;
    }
    
    public void setServiceUsage(ServiceUsage serviceUsage) {
        this.serviceUsage = serviceUsage;
        if (serviceUsage != null) {
            this.usageId = serviceUsage.getUsageId();
        }
    }
    
    // Utility methods
    public String getFormattedQuantity() {
        if (quantity == Math.floor(quantity)) {
            return String.format("%.0f", quantity);
        } else {
            return String.format("%.2f", quantity);
        }
    }
    
    public String getFormattedUnitPrice() {
        return String.format("$%.2f", unitPrice);
    }
    
    public String getFormattedLineTotal() {
        return String.format("$%.2f", lineTotal);
    }
    
    public String getItemTypeDisplay() {
        switch (itemType) {
            case ROOM:
                return "Room Charge";
            case SERVICE:
                return "Service";
            case TAX:
                return "Tax";
            case DISCOUNT:
                return "Discount";
            case EXTRA_CHARGE:
                return "Extra Charge";
            default:
                return "Other";
        }
    }
    
    public boolean isDiscount() {
        return itemType == ItemType.DISCOUNT;
    }
    
    public boolean isTax() {
        return itemType == ItemType.TAX;
    }
    
    public boolean isCharge() {
        return itemType == ItemType.ROOM || itemType == ItemType.SERVICE || itemType == ItemType.EXTRA_CHARGE;
    }
    
    public void calculateLineTotal() {
        this.lineTotal = quantity * unitPrice;
    }
    
    // Factory methods for common line item types
    public static InvoiceLineItem createRoomCharge(long invoiceId, String description, double amount) {
        return new InvoiceLineItem(invoiceId, ItemType.ROOM, description, amount);
    }
    
    public static InvoiceLineItem createServiceCharge(long invoiceId, String description, 
                                                     double quantity, double unitPrice, 
                                                     Integer serviceId, Long usageId) {
        InvoiceLineItem item = new InvoiceLineItem(invoiceId, ItemType.SERVICE, description, quantity, unitPrice);
        item.setServiceId(serviceId);
        item.setUsageId(usageId);
        return item;
    }
    
    public static InvoiceLineItem createTaxCharge(long invoiceId, String description, double amount) {
        return new InvoiceLineItem(invoiceId, ItemType.TAX, description, amount);
    }
    
    public static InvoiceLineItem createDiscount(long invoiceId, String description, double amount) {
        InvoiceLineItem item = new InvoiceLineItem(invoiceId, ItemType.DISCOUNT, description, -Math.abs(amount));
        return item;
    }
    
    public static InvoiceLineItem createExtraCharge(long invoiceId, String description, double amount) {
        return new InvoiceLineItem(invoiceId, ItemType.EXTRA_CHARGE, description, amount);
    }
    
    @Override
    public String toString() {
        return "InvoiceLineItem{" +
                "lineItemId=" + lineItemId +
                ", invoiceId=" + invoiceId +
                ", itemType=" + itemType +
                ", itemDescription='" + itemDescription + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", lineTotal=" + lineTotal +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        InvoiceLineItem that = (InvoiceLineItem) obj;
        return lineItemId == that.lineItemId;
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(lineItemId);
    }
}

