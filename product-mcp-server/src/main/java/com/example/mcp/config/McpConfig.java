package com.example.mcp.config;

import com.example.mcp.tool.ProductTools;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfig {

    @Bean
    ToolCallbackProvider productToolCallbacks(ProductTools tools) {
        return ToolCallbackProvider.from(ToolCallbacks.from(tools));
    }
}
