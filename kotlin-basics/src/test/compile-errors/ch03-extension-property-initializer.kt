package basics.ch03.extensionpropertyinitializer

data class OrderLine(val productName: String, val unitPrice: Int, val quantity: Int)

// snippet:ch03-extension-property-initializer begin
val OrderLine.discountRate: Int = 0
// snippet:ch03-extension-property-initializer end
