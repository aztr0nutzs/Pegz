package com.yourname.pegz.ui.shaders

import org.intellij.lang.annotations.Language

@Language("AGSL")
val BIO_GLOW_SHADER = """
    uniform float2 size;
    uniform float time;
    uniform float3 color;

    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / size;
        float2 center = float2(0.5, 0.5);
        float dist = distance(uv, center);
        
        // Create a pulsing effect based on time
        float pulse = 0.8 + 0.2 * sin(time * 3.0);
        
        // Define the inner liquid glow
        float glow = 1.0 - smoothstep(0.0, 0.4 * pulse, dist);
        
        // Add a "rim" highlight to simulate glass
        float rim = smoothstep(0.45, 0.5, dist) * smoothstep(0.55, 0.5, dist);
        
        float3 finalColor = color * glow + (float3(1.0) * rim * 0.5);
        float alpha = smoothstep(0.5, 0.48, dist); // Clean circular cutout
        
        return half4(finalColor * alpha, alpha);
    }
""".trimIndent()