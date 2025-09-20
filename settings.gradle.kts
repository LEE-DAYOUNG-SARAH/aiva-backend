rootProject.name = "aiva-backend"

include(
    "services:user-service",
    "services:chat-service", 
    "services:community-service",
    "services:notification-service",
    "services:subscription-service",
    "infrastructure:gateway",
    "shared:common",
    "shared:security"
)
