package com.aluracursos.screenmatch.principal;

import com.aluracursos.screenmatch.model.*;
import com.aluracursos.screenmatch.repository.SerieRepository;
import com.aluracursos.screenmatch.service.ConsumoApi;
import com.aluracursos.screenmatch.service.ConvierteDatos;

import java.util.*;
import java.util.stream.Collectors;

public class PrincipalOrdenada {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoApi consumoApi = new ConsumoApi();
    private final String URL_BASE = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=92c9d287";
    private ConvierteDatos conversor = new ConvierteDatos();
    private List<DatosSerie> datosSeries= new ArrayList<>();
    private SerieRepository repositorio;
    List<Serie> series;
    Optional<Serie> serieBuscada;
    public PrincipalOrdenada(SerieRepository repository) {
        this.repositorio = repository;
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    1 - Buscar series 
                    2 - Buscar episodios
                    3 - Mostrar series buscadas
                    4 - Buscar serie por titulo  
                    5 - Top 5 mejores series    
                    6 - Buscar series por categoria
                    7 - Buscar por total de temporada y evaluacion
                    8 - Buscar episodio por titulo  
                    9 - Top 5 episodios por serie
                    0 - Salir
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1-> buscarSerieWeb();
                case 2-> buscarEpisodioPorSerie();
                case 3-> mostrasSeriesBuscadas();
                case 4-> buscarSeriesPorTitulo();
                case 5-> buscarTop5Series();
                case 6 -> buscarSeriesporCategorias();
                case 7 -> buscarPorTemporadaYEvaluacion();
                case 8 -> buscarEpisodiosPorTitulo();
                case 9 -> buscarTop5Episodios();
                case 0-> System.out.println("Cerrando la aplicación...");
                default -> System.out.println("Opción inválida");
            }
        }

    }



    private DatosSerie getDatosSerie() {
        System.out.println("Escribe el nombre de la serie que deseas buscar");
        var nombreSerie = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + nombreSerie.replace(" ", "+") + API_KEY);
        System.out.println(json);
        DatosSerie datos = conversor.obtenerDatos(json, DatosSerie.class);
        return datos;
    }
    private void buscarEpisodioPorSerie() {
        mostrasSeriesBuscadas();
        System.out.println("Escribe el nombre de la serie de la cual quieres ver los episodios");
        var nombreSerie= teclado.nextLine();

        Optional<Serie> serie=series.stream()
                .filter(s-> s.getTitulo().toLowerCase().contains(nombreSerie.toLowerCase()))
                .findFirst();
        if (serie.isPresent()){
            var serieEncontrada = serie.get();
            List<DatosTemporadas> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalDeTemporadas(); i++) {
                var json = consumoApi.obtenerDatos(URL_BASE + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DatosTemporadas datosTemporada = conversor.obtenerDatos(json, DatosTemporadas.class);
                temporadas.add(datosTemporada);
            }
            temporadas.forEach(System.out::println);
            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d-> d.episodios().stream()
                            .map(e->new Episodio(d.numero(),e)))
                    .collect(Collectors.toList());
            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);
        }

    }
    private void buscarSerieWeb() {
        DatosSerie datos = getDatosSerie();
        Serie serie = new Serie(datos);
        repositorio.save(serie);
        //datosSeries.add(datos);

        System.out.println(datos);
    }

    private void mostrasSeriesBuscadas() {
         series= repositorio.findAll();

        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }
    private void buscarSeriesPorTitulo() {

        System.out.println("Escribe el nombre de la serie que deseas buscar");
        var nombreSerie= teclado.nextLine();
        serieBuscada= repositorio.findByTituloContainsIgnoreCase(nombreSerie);
        if (serieBuscada.isPresent()){
            System.out.println("La serie buscada es: "+serieBuscada.get() );
        }else {
            System.out.println("Serie no encontrada ");
        }
    }
    private  void buscarTop5Series(){
        List<Serie> topSerie= repositorio.findTop5ByOrderByEvaluacionDesc();
        topSerie.forEach(s->
                System.out.println("Serie: "+s.getTitulo()+ "Evaluacion: "+s.getEvaluacion()));
    }

    private void buscarSeriesporCategorias(){
        System.out.println("Escriba el genero/categoria de la serie que desea buscar");
        var genero = teclado.nextLine();
        var categoria = Categoria.fromEspañol(genero);
        List<Serie> seriesPorCategoria = repositorio.findByGenero(categoria);

        System.out.println("Las series de la categoria "+ genero);
        seriesPorCategoria.forEach(System.out::println);
    }

    private void buscarPorTemporadaYEvaluacion(){
        System.out.println("¿Filtrar séries con cuántas temporadas? ");

        var temporada = Integer.valueOf(teclado.nextLine());
        System.out.println("¿Com evaluación apartir de cuál valor? ");

        var evaluacion = Double.valueOf(teclado.nextLine());

        List<Serie> temporadaYevaluacion = repositorio.seriesPorTemporadaYEvaluacion(temporada,evaluacion);
        temporadaYevaluacion.forEach(t-> System.out.println(t.getTitulo() + "  - evaluacion: " + t.getEvaluacion()));
    }

    private void buscarEpisodiosPorTitulo(){
        System.out.println("Escribe el nombre del episodio");
        var nombreEpisodio= teclado.nextLine();
        List<Episodio> episodiosEncontrados= repositorio.episodioPorNombre(nombreEpisodio);
        episodiosEncontrados.forEach(e ->
                System.out.printf("Serie: %s Temporada: %s Episodio: %s Evaluaciòn %s \n",
                e.getSerie().getTitulo(), e.getTemporada(), e.getNumeroEpisodio() , e.getEvaluacion()));
    }

    private  void buscarTop5Episodios(){
        buscarSeriesPorTitulo();
        if (serieBuscada.isPresent()){
            Serie serie= serieBuscada.get();
            List<Episodio> topEpisodio = repositorio.top5Episodios(serie);
            topEpisodio.forEach(e ->
                    System.out.printf("Serie: %s - Temporada: %s - Episodio: %s - Evaluaciòn %s \n",
                            e.getSerie().getTitulo(), e.getTemporada(), e.getTitulo() , e.getEvaluacion()));
        }
    }
}
