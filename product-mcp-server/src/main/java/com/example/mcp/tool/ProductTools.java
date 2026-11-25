package com.example.mcp.tool;

import com.example.mcp.client.ProductApiClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** The MCP tool surface. Each tool delegates to {@link ProductApiClient}. */
@Component
public class ProductTools {

    private final ProductApiClient api;

    public ProductTools(ProductApiClient api) {
        this.api = api;
    }

    @Tool(description = "List all products.")
    public Object listProducts() {
        return api.list();
    }

    @Tool(description = "Get a single product by its id.")
    public Object getProduct(@ToolParam(description = "Product id") Long id) {
        return api.get(id);
    }

    @Tool(description = "Create a new product. Available to USER and ADMIN.")
    public Object createProduct(
            @ToolParam(description = "Product name") String name,
            @ToolParam(description = "Product description", required = false) String description,
            @ToolParam(description = "Price, 0 or greater, max 2 decimals") BigDecimal price,
            @ToolParam(description = "Stock quantity, 0 or greater") Integer quantity) {
        return api.create(name, description, price, quantity == null ? 0 : quantity);
    }

    @Tool(description = "Delete a product by id. Only ADMIN tokens may do this; USER tokens get a 403.")
    public Object deleteProduct(@ToolParam(description = "Product id") Long id) {
        return api.delete(id);
    }
}
