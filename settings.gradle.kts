rootProject.name = "aiva-backend"

include(
    "services:user-service",
    "services:chat-service", 
    "services:community-service",
    "services:notification-service",
    "services:subscription-service",
    "services:batch-service",
    "infrastructure:gateway",
    "shared:common",
    "shared:security"
)
