-- TP N° 2 - Sistema de Gestión de Biblioteca
-- Script 1: creación de la base de datos.
-- Ejecutar conectado a la base "postgres":
--   psql -U postgres -f sql/01_crear_base.sql

DROP DATABASE IF EXISTS gestion_biblioteca;
CREATE DATABASE gestion_biblioteca ENCODING 'UTF8';
