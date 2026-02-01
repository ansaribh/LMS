#!/bin/bash
# Wait for Keycloak to be ready
echo "Waiting for Keycloak to start..."
until curl -sf http://localhost:8080/health/ready > /dev/null 2>&1; do
    sleep 5
done
echo "Keycloak is ready!"

# Authenticate
/opt/keycloak/bin/kcadm.sh config credentials \
    --server http://localhost:8080 \
    --realm master \
    --user $KEYCLOAK_ADMIN \
    --password $KEYCLOAK_ADMIN_PASSWORD

# Disable SSL for master realm (development only)
/opt/keycloak/bin/kcadm.sh update realms/master -s sslRequired=NONE

echo "Master realm SSL requirement disabled for development"
