-- liquibase formatted sql
-- changeset minhln:1.11

-- Add new field `code` to table pm_patient
alter table pm_patient add column if not exists code varchar(255);
