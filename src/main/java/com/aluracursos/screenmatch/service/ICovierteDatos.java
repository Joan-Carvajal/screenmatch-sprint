package com.aluracursos.screenmatch.service;

public interface ICovierteDatos {

    <T> T obtenerDatos(String json, Class<T> clase);
}
