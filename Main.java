
import java.util.*;
import java.io.*;

class Paciente{
    private String nombre, apellido, id, estado, area;
    private int categoria;
    private long tiempoLlegada;
    private Stack<String> historialCambios = new Stack<>();

    public Paciente(String nombre,String apellido,String id,int categoria,long tiempoLlegada,String area){
        this.nombre=nombre;
        this.apellido=apellido;
        this.id=id;
        this.categoria=categoria;
        this.tiempoLlegada=tiempoLlegada;
        this.estado="en_espera";
        this.area=area;
    }
    public long tiempoEsperaActual(){
        return (System.currentTimeMillis()/1000-tiempoLlegada)/60;
    }
    public void registrarCambio(String descripcion){
        historialCambios.push(descripcion);
    }
    public String obtenerUltimoCambio(){
        return historialCambios.pop();
    }
    public int getCategoria(){
        return categoria;
    }
    public void setCategoria(int c){
        categoria=c;
    }
    public long getTiempoLlegada(){
        return tiempoLlegada;
    }
    public String getEstado(){
        return estado;
    }
    public void setEstado(String e){
        estado=e;
    }
    public String getId(){
        return id;
    }
    public String getArea(){
        return area;
    }
}
class AreaAtencion{
    private String nombre;
    private PriorityQueue<Paciente> pacientesHeap;
    private int capacidadMaxima;

    public AreaAtencion(String nom,int cap){
        nombre=nom;
        capacidadMaxima=cap;
        pacientesHeap = new PriorityQueue<>((p1, p2) -> {
            double espera1=(System.currentTimeMillis()/1000-p1.getTiempoLlegada())/60.0;
            double espera2=(System.currentTimeMillis()/1000-p2.getTiempoLlegada())/60.0;
            double prioridad1=p1.getCategoria()-espera1/100.0;
            double prioridad2=p2.getCategoria()-espera2/100.0;
            return Double.compare(prioridad1, prioridad2);
        });
    }
    public void ingresarPaciente(Paciente p){
        if(!estaSaturada()){
            pacientesHeap.add(p);
        }
        else{
            System.out.print("Capacidad maxima alcanzada.");
        }
    }
    public Paciente atenderPaciente(){
        return pacientesHeap.poll();
    }
    public boolean estaSaturada(){
        return pacientesHeap.size()>=capacidadMaxima;
    }
    public List<Paciente> obtenerPacientesPorHeapSort(){
        List<Paciente> obPS = new ArrayList<>(pacientesHeap);
        obPS.sort(pacientesHeap.comparator());
        return obPS;
    }
}
class Hospital{
    private Map<String,Paciente> pacientesTotales = new HashMap<>();
    private PriorityQueue<Paciente> colaAtencion = new PriorityQueue<>((p1, p2) -> {
        double espera1=(System.currentTimeMillis()/1000-p1.getTiempoLlegada())/60.0;
        double espera2=(System.currentTimeMillis()/1000-p2.getTiempoLlegada())/60.0;
        double prioridad1=p1.getCategoria()-espera1/100.0;
        double prioridad2=p2.getCategoria()-espera2/100.0;
        return Double.compare(prioridad1, prioridad2);
    });
    private Map<String,AreaAtencion> areasAtencion = Map.of(
        "SAPU",new AreaAtencion("SAPU",100),"urgencia_adulto",new AreaAtencion("urgencia_adulto",100),"infantil", new AreaAtencion("infantil",100));
    private List<Paciente> pacientesAtendidos = new ArrayList<>();
    private List<Paciente> pacientesFueraDeTiempo = new ArrayList<>();
    
    public void registrarPaciente(Paciente p){
        pacientesTotales.put(p.getId(),p);
        colaAtencion.add(p);
    }
    public void reasignarCategoria(String id,int nuevaCategoria){
        Paciente pa=pacientesTotales.get(id);
        if(pa!=null){
            colaAtencion.remove(pa);
            int anterior=pa.getCategoria();
            pa.setCategoria(nuevaCategoria);
            pa.registrarCambio("Cambio de C"+anterior+" a C"+nuevaCategoria);
            colaAtencion.add(pa);
        }
    }
    public Paciente atenderSiguiente(){
        Paciente p=colaAtencion.peek();
        if(p==null){
            return null;
        }
        long tim=p.tiempoEsperaActual();
        int cat=p.getCategoria();
        boolean temp = switch(cat){
            case 1 -> true;
            case 2 -> tim>30;
            case 3 -> tim>90;
            case 4 -> tim>180;
            case 5 -> false;
            default -> false;
        };
        if(temp){
            pacientesFueraDeTiempo.add(p);
        }
        p=colaAtencion.poll();
        if(p==null){
            return null;
        }
        AreaAtencion a=areasAtencion.get(p.getArea());
        if(!a.estaSaturada()){
            a.ingresarPaciente(p);
            Paciente lito=a.atenderPaciente();
            lito.setEstado("atendido");
            pacientesAtendidos.add(lito);
            return lito;
        }
        return null;
    }
    public List<Paciente> obtenerPacientesPorCategoria(int categoria){
        List<Paciente> rar = new ArrayList<>();
        for(Paciente pac : colaAtencion){
            if(pac.getCategoria()==categoria){
                rar.add(pac);
            }
        }
        return rar;
    }
    public List<Paciente> getpacientesFuera(){
        return pacientesFueraDeTiempo;
    }
    public AreaAtencion obtenerArea(String n){ 
        return areasAtencion.get(n); 
    }
    public List<Paciente> getpacientesAtendidos(){ 
        return pacientesAtendidos;
    }
    public Map<String, Paciente> getTotales(){ 
        return pacientesTotales;
    }
    public PriorityQueue<Paciente> getcolaAtencion(){ 
        return colaAtencion;
    }
}
class GeneradorPacientes{
    private static final String[] nombre={"Ana","Luis","Carlos","Sofia","Mario","Lucia"};
    private static final String[] apellido={"Rojas","Muñoz","Vera","Lopez"};
    private static final String[] area={"SAPU", "urgencia_adulto", "infantil"};

    public static List<Paciente> generar(int N,long t0){
        List<Paciente> ps = new ArrayList<>();
        Random r = new Random();
        for(int i=0;i<N;i++){
            String nom=nombre[r.nextInt(nombre.length)];
            String ape=apellido[r.nextInt(apellido.length)];
            String id="ID"+i;
            int cat=probabilidad(r.nextDouble());
            long tim=t0+(i*600);
            String ar=area[r.nextInt(area.length)];
            ps.add(new Paciente(nom,ape,id,cat,tim,ar));
        }
        return ps;
    }
    private static int probabilidad(double prob){
        if(prob<0.10) return 1;
        if(prob<0.25) return 2;
        if(prob<0.43) return 3;
        if(prob<0.70) return 4;
        return 5;
    }
    public static void guardar(List<Paciente> ps,String nom){
        try(FileWriter fw = new FileWriter(nom)){
            for(Paciente p : ps){
                fw.write(p.getId()+" "+p.getCategoria()+" "+p.getArea()+" "+p.getEstado());
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
    public static void guardarPacientesFueraDeTiempo(List<Paciente> lista,String archivo){
    try(FileWriter fw=new FileWriter(archivo)){
        for(Paciente p:lista){
            fw.write("ID: "+p.getId()+", C"+p.getCategoria()+", Espera: "+p.tiempoEsperaActual());
        }
    }
    catch(IOException e){
        e.printStackTrace();
    }
}
}
class SimuladorUrgencia{
    private Hospital h = new Hospital();
    private List<Paciente> pass;
    private int cont=0;
    public SimuladorUrgencia(List<Paciente> pass){
        this.pass=pass;
    }
    public void simular(int pacientesPorDia){
        int min=0;
        int x=0;
        while(min<1440 && x<pass.size()){
            if(min%10==0){
                h.registrarPaciente(pass.get(x++));
                cont++;
            }
            if(min%15==0){ 
                h.atenderSiguiente();
            }
            if(cont>=3){
                h.atenderSiguiente();
                h.atenderSiguiente();
                cont=0;
            }
            min++;
        }
    }
    public void resumen(){
        System.out.println("Total de pacientes atendidos: "+h.getpacientesAtendidos().size());
    }
}
public class Main{
    public static void main(String[] args){
        long[] sumaPorCategoria=new long[6];
        int[] conteoPorCategoria=new int[6];
        long t=System.currentTimeMillis()/1000- 1440*60;
        String idC4=null;
        long esperaC4=-1;
        List<Paciente> acumuladosFueraDeTiempo=new ArrayList<>();

        for(int i=1;i<=15;i++){
            List<Paciente> ps=GeneradorPacientes.generar(144,t-i);
            if(i==1){
            GeneradorPacientes.guardar(ps,"Pacientes_24h_simulacion.txt");
            }
            Hospital h=new Hospital();
            for(Paciente p : ps){
                h.registrarPaciente(p);
            }
            Paciente malCat=null;
            for(Paciente p : ps){
                if(p.getCategoria()==3){
                    malCat=p;
                    h.reasignarCategoria(p.getId(),1);
                    break;
                }
            }
            if(i==1 && malCat!=null){
                System.out.println("Paciente corregido = ID: "+malCat.getId()+" Historial: "+malCat.obtenerUltimoCambio());
            }
            int min=0;
            int atendidos=0;
            while(min<1440 && atendidos<144){
                if(min%15==0){
                    Paciente p=h.atenderSiguiente();
                    if(p!=null){
                        long espera=p.tiempoEsperaActual();
                        int cat=p.getCategoria();
                        sumaPorCategoria[cat]+=espera;
                        conteoPorCategoria[cat]++;
                        if(idC4==null && cat==4){
                            idC4=p.getId();
                        }
                        if(idC4!=null && p.getId().equals(idC4)){
                            esperaC4=espera;
                        }
                        atendidos++;
                    }
                }
                min++;
            }
            if(i==1 && idC4!=null){
                System.out.println("paciente C4 = ID: "+idC4+" Tiempo de espera: "+esperaC4);
            }
            acumuladosFueraDeTiempo.addAll(h.getpacientesFuera());
            List<Paciente> c3=h.obtenerPacientesPorCategoria(3);
            System.out.println("pacientes en espera de categoria C3: "+c3.size());
            AreaAtencion a=h.obtenerArea("urgencia_adulto");
            List<Paciente> ordenados=a.obtenerPacientesPorHeapSort();
            System.out.println("pacientes en urgencia_adulto ordenados por heapsort: "+ordenados.size());
        }
        System.out.println("Pacientes que excedieron su tiempo:");
        for(Paciente p : acumuladosFueraDeTiempo){
            System.out.println("ID: "+p.getId()+", Categoria: C"+p.getCategoria()+", Espera: "+p.tiempoEsperaActual()+" min");
        }
        GeneradorPacientes.guardarPacientesFueraDeTiempo(acumuladosFueraDeTiempo,"Pacientes_fuera_tiempo.txt");
        System.out.println("Promedios de espera por categoria tras 15 simulaciones:");
        for(int c=1;c<=5;c++){
            if(conteoPorCategoria[c]>0){
                double prom=(double)sumaPorCategoria[c]/conteoPorCategoria[c];
                System.out.println("C"+c+" = "+prom+" min, pacientes:"+conteoPorCategoria[c]);
            }
            else{
                System.out.println("C"+c+" = no hay pacientes.");
            }
        }
    }
}