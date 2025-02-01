provider "aws" {
  region = "eu-central-1"
}

# ✅ Create an IAM Role for Lambda Execution
resource "aws_iam_role" "lambda_execution_role" {
  name = "lambda-dynamodb-access-role1"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Principal = {
        Service = "lambda.amazonaws.com"
      }
      Action = "sts:AssumeRole"
    }]
  })
}

# ✅ Attach AWS Managed Policies for Lambda Execution and DynamoDB
resource "aws_iam_role_policy_attachment" "attach_lambda_basic_policy" {
  role       = aws_iam_role.lambda_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_iam_role_policy_attachment" "attach_dynamodb_full_access" {
  role       = aws_iam_role.lambda_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess"
}

# ✅ Deploy Lambda Function
resource "aws_lambda_function" "example_lambda" {
  filename         = "../target/config-service-1.0-SNAPSHOT-lambda-package.zip" # Path to your ZIP file
  function_name    = "firstfunction"
  role             = aws_iam_role.lambda_execution_role.arn # ✅ Dynamically attach the IAM role
  handler          = "com.parkhere.configuration.StreamLambdaHandler::handleRequest" # ✅ Update based on your handler
  runtime          = "java17"
  source_code_hash = filebase64sha256("../target/config-service-1.0-SNAPSHOT-lambda-package.zip")
  timeout          = 30 # Increase the timeout to 30 seconds


}
