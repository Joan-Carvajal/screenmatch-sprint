package com.aluracursos.screenmatch.principal;

import com.aluracursos.screenmatch.model.DatosEpisodio;
import com.aluracursos.screenmatch.model.DatosSerie;
import com.aluracursos.screenmatch.model.DatosTemporadas;
import com.aluracursos.screenmatch.model.Episodio;
import com.aluracursos.screenmatch.service.ConsumoApi;
import com.aluracursos.screenmatch.service.ConvierteDatos;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner scanner = new Scanner(System.in);
    private ConsumoApi consumoApi = new ConsumoApi();
    private final String URL_BASE= "https://www.omdbapi.com/?t=";
    private final String API_KEY="&apikey=92c9d287";
    private ConvierteDatos conversor = new ConvierteDatos();
    public void muestraElMenu(){
        System.out.println("Por favor escribe le nombre de la serie que deseas buscar");
        var nombreSerie = scanner.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE+nombreSerie.replace(" ", "+")+API_KEY);
        var datos = conversor.obtenerDatos(json, DatosSerie.class);
        System.out.println(datos);
        // buscas los datos de la temporada

        List<DatosTemporadas> temporadas = new ArrayList<>();
        for (int i = 1; i <= datos.totalDeTemporadas() ; i++) {
            json = consumoApi.obtenerDatos(URL_BASE+nombreSerie.replace(" ", "+")+"&Season="+i+API_KEY);
            var datosTemporadas = conversor.obtenerDatos(json,DatosTemporadas.class);
            temporadas.add(datosTemporadas);
        }
      //  temporadas.forEach(System.out::println);

        // Mostrar  solo el titulo de los episodios para la temporada
//        for (int i = 0; i <datos.totalDeTemporadas() ; i++) {
//            List<DatosEpisodio> episodiosTemporada= temporadas.get(i).episodios();
//            for (int j = 0; j < episodiosTemporada.size() ; j++) {
//                System.out.println(episodiosTemporada.get(j).titulo());
//            }
//        }
       // temporadas.forEach(t -> t.episodios().forEach(e-> System.out.println(e.titulo())));

        // Convertir todas las informaciones a una lista de tipo de DatosEpisodio

        List<DatosEpisodio> datosEpisodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream())
                .collect(Collectors.toList());

        // Top 5 de los mejores cap
//        System.out.println("Top 5 episodios");
//        datosEpisodios.stream()
//                .filter(e -> !e.evaluacion().equalsIgnoreCase("N/A"))
//                .peek(e -> System.out.println("Primer filtro (N/A)"+ e))
//                .sorted(Comparator.comparing(DatosEpisodio::evaluacion).reversed())
//                .peek(e -> System.out.println("Segundo Ordenacion (M/m) "+ e))
//                .map(e -> e.titulo().toUpperCase())
//                .peek(e -> System.out.println("Tercer Filtro Mayusculas (m/M)"+ e))
//                .limit(5)
//                .forEach(System.out::println);

        // Convirtiendo los datos  a una lista del tipo Episodio

        List<Episodio> episodios= temporadas.stream()
                .flatMap(t-> t.episodios().stream()
                        .map(d-> new Episodio(t.numero(),d)))
                .collect(Collectors.toList());

      //  episodios.forEach(System.out::println);

        // Busqueda de episodios a partir de x año

//        System.out.println("Por favor indica el año del cual deseas ver los episodios");
//
//        var fecha = scanner.nextInt();
//        scanner.nextLine();
//
//        LocalDate fechaBusqueda = LocalDate.of(fecha,1,1);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//        episodios.stream()
//                .filter(e-> e.getFechaDeLanzamiento() != null && e.getFechaDeLanzamiento().isAfter(fechaBusqueda))
//                .forEach(e -> System.out.println(
//                        "Temporada " + e.getTemporada()+
//                                " Episodio " + e.getTitulo() +
//                                " Fecha de Lanzamiento " + e.getFechaDeLanzamiento().format(dtf)
//                ));


        // Busqueda espisodios por un pedazo del titulo

//        System.out.println("Por favor escriba el titulo del episodio que desea ver");
//        var pedazoTitulo = scanner.nextLine();
//        Optional<Episodio> episodioBuscado =  episodios.stream()
//                .filter(e ->e.getTitulo().toUpperCase().contains(pedazoTitulo.toUpperCase()))
//                .findFirst();
//
//        if (episodioBuscado.isPresent()){
//            System.out.println(" Espisodio encontrado ");
//            System.out.println("Los datos son: "+episodioBuscado.get());
//        }else {
//            System.out.println("Episodio no encontrado");
//        }


        Map<Integer, Double> evaluacionesPorTemporada= episodios.stream()
                .filter(e ->e.getEvaluacion() >0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada,
                        Collectors.averagingDouble(Episodio::getEvaluacion)));
        System.out.println(evaluacionesPorTemporada);

        DoubleSummaryStatistics est  = episodios.stream()
                .filter(e ->e.getEvaluacion() >0.0)
                .collect(Collectors.summarizingDouble(Episodio::getEvaluacion));
        System.out.println("Media de las evaluciones"+ est.getAverage());
        System.out.println("Episodio mejor evaluado"+ est.getMax());
        System.out.println("Episodio menor evaluado "+ est.getMin());


    }
}
