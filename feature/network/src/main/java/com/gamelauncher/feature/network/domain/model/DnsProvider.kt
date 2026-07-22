package com.gamelauncher.feature.network.domain.model

/**
 * DnsProvider — Represents standard Android Private DNS provider configurations.
 * All providers use standard Android 9+ Private DNS (TLS) hostnames.
 */
data class DnsProvider(
    val id: String,
    val name: String,
    val description: String,
    val hostname: String?,
    val isSystemDefault: Boolean = false
) {
    companion object {
        val DEFAULT_PROVIDERS = listOf(
            DnsProvider(
                id = "default",
                name = "Automatic (System Default)",
                description = "Use standard ISP/carrier DNS provided by network operator.",
                hostname = null,
                isSystemDefault = true
            ),
            DnsProvider(
                id = "cloudflare",
                name = "Cloudflare 1.1.1.1",
                description = "Lowest latency DNS optimized for online gaming and privacy.",
                hostname = "one.one.one.one"
            ),
            DnsProvider(
                id = "google",
                name = "Google Public DNS",
                description = "High reliability global Anycast DNS infrastructure.",
                hostname = "dns.google"
            ),
            DnsProvider(
                id = "quad9",
                name = "Quad9 Secure DNS",
                description = "Threat-blocking secure DNS with privacy protection.",
                hostname = "dns.quad9.net"
            ),
            DnsProvider(
                id = "adguard",
                name = "AdGuard DNS",
                description = "Blocks ad domains and tracking servers.",
                hostname = "dns.adguard-dns.com"
            )
        )
    }
}
