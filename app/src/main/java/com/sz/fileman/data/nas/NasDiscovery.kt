package com.sz.fileman.data.nas

import com.sz.fileman.domain.model.NasConnection
import jcifs.smb.SmbException
import jcifs.smb.SmbFile
import timber.log.Timber
import java.net.InetAddress
import java.net.NetworkInterface
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Discovers NAS devices on the local network.
 * Uses SMB protocol to scan for available servers.
 */
@Singleton
class NasDiscovery @Inject constructor() {
    
    /**
     * Discover NAS devices on the local network.
     * Returns a list of discovered NAS connections.
     * Note: This is a basic implementation. For production, consider using
     * proper network discovery protocols like mDNS/Bonjour or UPnP.
     */
    suspend fun discover(): List<DiscoveredNasDevice> {
        Timber.d("Starting NAS discovery")
        
        val discoveredDevices = mutableListOf<DiscoveredNasDevice>()
        
        try {
            // Get local network interfaces
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
                .toList()
                .filter { it.isUp && !it.isLoopback }
            
            Timber.d("Found ${networkInterfaces.size} network interfaces")
            
            // Scan common NAS IP ranges
            for (networkInterface in networkInterfaces) {
                val interfaceAddresses = networkInterface.interfaceAddresses
                
                for (address in interfaceAddresses) {
                    val inetAddress = address.address
                    if (inetAddress is InetAddress && !inetAddress.isLoopbackAddress) {
                        val hostAddress = inetAddress.hostAddress
                        if (hostAddress != null && (hostAddress.startsWith("192.168.") || hostAddress.startsWith("10."))) {
                            Timber.d("Scanning network: $hostAddress")
                            
                            // Try to discover SMB servers on this network
                            val devices = discoverSmbServers(hostAddress)
                            discoveredDevices.addAll(devices)
                        }
                    }
                }
            }
            
            Timber.d("Discovery completed. Found ${discoveredDevices.size} devices")
        } catch (e: Exception) {
            Timber.e(e, "Error during NAS discovery")
        }
        
        return discoveredDevices
    }
    
    /**
     * Try to discover SMB servers on a specific network.
     * This scans the local subnet for SMB servers.
     */
    private suspend fun discoverSmbServers(networkAddress: String): List<DiscoveredNasDevice> {
        val devices = mutableListOf<DiscoveredNasDevice>()
        
        try {
            // Extract network prefix (e.g., 192.168.1)
            val parts = networkAddress.split(".")
            if (parts.size != 4) return devices
            
            val networkPrefix = "${parts[0]}.${parts[1]}.${parts[2]}"
            
            // Scan a limited range of IPs (1-50 to avoid long scan times)
            // In production, you might want to:
            // 1. Use mDNS/Bonjour for zero-config discovery
            // 2. Use UPnP for device discovery
            // 3. Use a background service for continuous scanning
            for (i in 1..50) {
                val ipAddress = "$networkPrefix.$i"
                
                try {
                    // Try to connect to SMB server
                    val smbUrl = "smb://$ipAddress/"
                    val smbFile = SmbFile(smbUrl)
                    
                    // Check if this is an SMB server
                    if (smbFile.exists()) {
                        // Try to list shares
                        val shares = smbFile.listFiles()
                        if (!shares.isNullOrEmpty()) {
                            devices.add(
                                DiscoveredNasDevice(
                                    host = ipAddress,
                                    name = "NAS-$ipAddress",
                                    shares = shares.map { it.name }.take(5) // Limit to 5 shares
                                )
                            )
                            Timber.d("Found SMB server at: $ipAddress")
                        }
                    }
                } catch (e: SmbException) {
                    // Not an SMB server, continue
                } catch (e: Exception) {
                    // Other error, continue
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error scanning network: $networkAddress")
        }
        
        return devices
    }
    
    /**
     * Test if a specific IP address has an SMB server running.
     * @param ipAddress The IP address to test
     * @param port The port to test (default 445 for SMB)
     * @return true if SMB server is detected
     */
    suspend fun testSmbServer(ipAddress: String, port: Int = 445): Boolean {
        return try {
            Timber.d("Testing SMB server at $ipAddress:$port")
            
            // Try to connect to SMB server
            val smbUrl = "smb://$ipAddress/"
            val smbFile = SmbFile(smbUrl)
            
            // Try to list shares - this will fail if not an SMB server
            val shares = smbFile.listFiles()
            !shares.isNullOrEmpty()
        } catch (e: SmbException) {
            Timber.d("Not an SMB server at $ipAddress")
            false
        } catch (e: Exception) {
            Timber.e(e, "Error testing SMB server at $ipAddress")
            false
        }
    }
    
    /**
     * Get available shares from an SMB server.
     * @param ipAddress The IP address of the SMB server
     * @return List of share names
     */
    suspend fun getShares(ipAddress: String): List<String> {
        return try {
            Timber.d("Getting shares from: $ipAddress")
            
            val smbUrl = "smb://$ipAddress/"
            val smbFile = SmbFile(smbUrl)
            val shares = smbFile.listFiles()
            
            shares?.map { it.name } ?: emptyList()
        } catch (e: Exception) {
            Timber.e(e, "Failed to get shares from: $ipAddress")
            emptyList()
        }
    }
}

/**
 * Represents a discovered NAS device.
 */
data class DiscoveredNasDevice(
    val host: String,
    val name: String,
    val shares: List<String> = emptyList()
) {
    /**
     * Convert to a NasConnection object.
     */
    fun toNasConnection(): NasConnection {
        return NasConnection(
            id = java.util.UUID.randomUUID().toString(),
            name = name,
            host = host,
            port = 445,
            username = "",
            password = "",
            isAnonymous = true,
            share = shares.firstOrNull() ?: ""
        )
    }
}
