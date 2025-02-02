terraform {
  backend "s3" {
    bucket         = "parkhere-terraform-state-bucket"
    key            = "config/terraform/state.tfstate"
    region         = "eu-central-1"
    dynamodb_table = "config-terraform-lock-table"
  }
}
