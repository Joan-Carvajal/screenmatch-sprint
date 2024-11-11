package com.aluracursos.screenmatch.principal;

import java.util.Arrays;
import java.util.List;

public class EjemploStreams {

    public void muestraEjemplos(){
        List<String> nombres = Arrays.asList("Brenda", "Luis", "Maria","Fernanda","Eric","Genesis");
        nombres.stream()
                .sorted()
                .limit(4)
                .filter(n -> n.startsWith("B"))
                .map(n -> n.toUpperCase())
                .forEach(System.out::println);
    }
}