# RSA Key Generator for JWT
Write-Host "Generating RSA keys for JWT" -ForegroundColor Green

# Create keys directory if it doesn't exist
$keysDir = "src/main/resources/keys"
if (!(Test-Path $keysDir)) {
    New-Item -ItemType Directory -Force -Path $keysDir | Out-Null
    Write-Host "Created folder: $keysDir" -ForegroundColor Green
}

# Generate RSA keys using .NET
try {
    Add-Type -AssemblyName System.Security.Cryptography

    # Generate RSA key pair (2048 bits)
    $rsa = [System.Security.Cryptography.RSA]::Create(2048)

    # Export private key in PEM format
    $privateKey = $rsa.ExportRSAPrivateKeyPem()
    $privateKeyPath = Join-Path $keysDir "private_key.pem"
    Set-Content -Path $privateKeyPath -Value $privateKey
    Write-Host "Private key created: $privateKeyPath" -ForegroundColor Green

    # Export public key in PEM format
    $publicKey = $rsa.ExportRSAPublicKeyPem()
    $publicKeyPath = Join-Path $keysDir "public_key.pem"
    Set-Content -Path $publicKeyPath -Value $publicKey
    Write-Host "Public key created: $publicKeyPath" -ForegroundColor Green

    Write-Host "`nKeys generated successfully!" -ForegroundColor Green

} catch {
    Write-Host "Error generating keys: $_" -ForegroundColor Red
}