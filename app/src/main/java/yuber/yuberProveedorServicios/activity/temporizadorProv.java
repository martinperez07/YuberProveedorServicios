package yuber.yuberProveedorServicios.activity;

public class temporizadorProv extends Thread {

    public void esperarXsegundos(int segundos) {
        try {
            Thread.sleep(segundos * 1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

}
