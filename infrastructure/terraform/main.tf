# AWS Provider 설정
terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

# Variables
variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-northeast-2"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "dev"
}

# SNS Topic for notifications
resource "aws_sns_topic" "aiva_notifications" {
  name = "aiva-notifications-${var.environment}"
  
  tags = {
    Environment = var.environment
    Project     = "aiva"
  }
}

# SQS Queue for notification processing
resource "aws_sqs_queue" "notification_queue" {
  name                      = "aiva-notification-queue-${var.environment}"
  delay_seconds             = 0
  max_message_size          = 262144
  message_retention_seconds = 1209600
  receive_wait_time_seconds = 10
  
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.notification_dlq.arn
    maxReceiveCount     = 3
  })
  
  tags = {
    Environment = var.environment
    Project     = "aiva"
  }
}

# Dead Letter Queue
resource "aws_sqs_queue" "notification_dlq" {
  name = "aiva-notification-dlq-${var.environment}"
  
  tags = {
    Environment = var.environment
    Project     = "aiva"
  }
}

# SNS to SQS subscription
resource "aws_sns_topic_subscription" "notification_sqs" {
  topic_arn = aws_sns_topic.aiva_notifications.arn
  protocol  = "sqs"
  endpoint  = aws_sqs_queue.notification_queue.arn
}

# SQS Queue Policy
resource "aws_sqs_queue_policy" "notification_queue_policy" {
  queue_url = aws_sqs_queue.notification_queue.id
  
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "sns.amazonaws.com"
        }
        Action   = "sqs:SendMessage"
        Resource = aws_sqs_queue.notification_queue.arn
        Condition = {
          ArnEquals = {
            "aws:SourceArn" = aws_sns_topic.aiva_notifications.arn
          }
        }
      }
    ]
  })
}

# IAM Role for Lambda
resource "aws_iam_role" "lambda_notification_role" {
  name = "aiva-lambda-notification-role-${var.environment}"
  
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })
}

# IAM Policy for Lambda
resource "aws_iam_role_policy" "lambda_notification_policy" {
  name = "aiva-lambda-notification-policy"
  role = aws_iam_role.lambda_notification_role.id
  
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ]
        Resource = "arn:aws:logs:*:*:*"
      },
      {
        Effect = "Allow"
        Action = [
          "sqs:ReceiveMessage",
          "sqs:DeleteMessage",
          "sqs:GetQueueAttributes"
        ]
        Resource = aws_sqs_queue.notification_queue.arn
      }
    ]
  })
}

# Lambda function placeholder (실제 코드는 별도 배포)
resource "aws_lambda_function" "notification_processor" {
  filename         = "notification_processor.zip"
  function_name    = "aiva-notification-processor-${var.environment}"
  role            = aws_iam_role.lambda_notification_role.arn
  handler         = "index.handler"
  runtime         = "nodejs18.x"
  timeout         = 30
  
  environment {
    variables = {
      ENVIRONMENT = var.environment
    }
  }
  
  tags = {
    Environment = var.environment
    Project     = "aiva"
  }
}

# Lambda Event Source Mapping
resource "aws_lambda_event_source_mapping" "notification_queue_trigger" {
  event_source_arn = aws_sqs_queue.notification_queue.arn
  function_name    = aws_lambda_function.notification_processor.arn
  batch_size       = 10
}

# Outputs
output "sns_topic_arn" {
  description = "SNS Topic ARN for notifications"
  value       = aws_sns_topic.aiva_notifications.arn
}

output "sqs_queue_url" {
  description = "SQS Queue URL for notifications"
  value       = aws_sqs_queue.notification_queue.id
}

output "lambda_function_name" {
  description = "Lambda function name for notification processing"
  value       = aws_lambda_function.notification_processor.function_name
}
