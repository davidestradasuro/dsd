import java.net.*;
import java.io.*;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;

public class MultMatDis{
  static double[][] ct;
  static class Worker extends Thread {
    Socket connection;
    Worker(Socket connection){
      this.connection = connection;
    }
    public void run(){
      //Algoritmo 1
      try {
        DataInputStream ins = new DataInputStream(connection.getInputStream());
        int nodo, n;
        nodo = ins.readInt();
        n = ins.readInt();
        //System.out.println("Recibiendo longitud "+n+" del nodo "+nodo);
        for(int i=0; i<n; i++){
          for(int j=0; j<n; j++){
            if(nodo==1){
              ct[i][j]=ins.readDouble();
            }else if(nodo==2){
              ct[i][j+n]=ins.readDouble();
            }else if(nodo==3){
              ct[i+n][j]=ins.readDouble();
            }
          }
        }


        ins.close();
        connection.close();
      } catch (IOException e){
        e.printStackTrace();
      }
    }

  }

  public static void main(String args[]) throws Exception {
    int nodo = 0, n;
    double[][] a,b,c,a1,a2,b1,b2,c4;
    a1 = new double[2][2];
    a2 = new double[2][2];
    b1 = new double[2][2];
    b2 = new double[2][2];
    try{
      nodo = Integer.valueOf(args[0]);
    }catch(Exception e){
      System.err.println("No se indicó el nodo");
			System.exit(0);
    }
    if(nodo ==0){
      //server
      Scanner read=new Scanner(System.in);
      System.out.println("Ingrese el tamanio de la matriz");
      n = read.nextInt();
      if(n<0){
        System.err.println("La matriz es muy chica");
        System.exit(0);
      }
      a = new double[n][n];
      b = new double[n][n];
      c = new double[n][n];
      ct = new double[n][n];
      //inicializar matrices
      for(int i=0; i<n; i++){
        for(int j=0; j<n; j++){
          a[i][j]= (double) i+5*j;
          b[i][j]= (double) 5*i-j;
        }
      }
      //imprimirMat(a, "A");
      //imprimirMat(b, "B");
      trasponerMat(b);
      //imprimirMat(b, "Bt");
      // crear 4 matrices por matriz dividir el contenido
      a1 = new double[n/2][n];
      a2 = new double[n/2][n];
      b1 = new double[n/2][n];
      b2 = new double[n/2][n];
      c4 = new double[n/2][n/2];
      for(int i=0; i<n;i++){
        for(int j=0; j<n;j++){
          if(i<n/2){
            a1[i][j]=a[i][j];
            b1[i][j]=b[i][j];
          }
          else{
            a2[i-n/2][j]=a[i][j];
            b2[i-n/2][j]=b[i][j];
          }

        }
      }

      ServerSocket server;
      Worker w[] = new Worker[3];
      for (int i=0;i<3;i++){
        server = new ServerSocket(50000+i+1);
        Socket client = server.accept();
        w[i] = new Worker(client);
        DataOutputStream salida = new DataOutputStream(client.getOutputStream());

        if(i==0){
          //enviar a1
          salida.writeInt(n/2);
          salida.writeInt(n);
          for(int l=0; l<n/2;l++){
            for(int j=0; j<n;j++){
              salida.writeDouble(a1[l][j]);
            }
          }
          //enviar b1
          for(int l=0; l<n/2;l++){
            for(int j=0; j<n;j++){
              salida.writeDouble(b1[l][j]);
            }
          }
        }else if(i==1){
          //enviar a1
          salida.writeInt(n/2);
          salida.writeInt(n);
          for(int l=0; l<n/2;l++){
            for(int j=0; j<n;j++){
              salida.writeDouble(a1[l][j]);
            }
          }
          //enviar b2
          for(int l=0; l<n/2;l++){
            for(int j=0; j<n;j++){
              salida.writeDouble(b2[l][j]);
            }
          }
        }else if(i==2){
          //enviar a1
          salida.writeInt(n/2);
          salida.writeInt(n);
          for(int l=0; l<n/2;l++){
            for(int j=0; j<n;j++){
              salida.writeDouble(a2[l][j]);
            }
          }
          //enviar b1
          for(int l=0; l<n/2;l++){
            for(int j=0; j<n;j++){
              salida.writeDouble(b1[l][j]);
            }
          }
        }
        w[i].start();
      }
      //calcular c4 desde nodo 0
      c4 = multMat(b2,a2);
      //imprimirMat(c4, "C4");

      for(int i=0; i<c4.length; i++){
        for(int j=0; j<c4.length; j++){
          //no se almacena en la matriz final
          ct[i+(c4.length)][j+(c4.length)]=c4[i][j];
        }
      }

      for(int i=0;i<3;i++){
        w[i].join();
      }

      //imprimirMat(ct, "CT");
      //calcular checksum matriz ct
      System.out.println("Checksum: "+checksum(ct));
      if(n==8){
        imprimirMat(a, "A");
        imprimirMat(b, "Bt");
        imprimirMat(ct, "C");
      }

    }else{
      Socket connection = null;
      for(;;)
			try {
        //ip del nodo 0
				connection = new Socket("52.190.7.82",50000+nodo);
				break;
			} catch (Exception e) {
				Thread.sleep(100);
			}


      DataInputStream inc = new DataInputStream(connection.getInputStream());

      int m1,n1;
      m1 = inc.readInt();
      n1 = inc.readInt();
      a = new double[m1][n1];
      c = new double[n1][n1];

      for(int i=0;i<m1;i++){
        for(int j=0;j<n1;j++){
          a[i][j]=inc.readDouble();
        }
      }
      b = new double[m1][n1];
      for(int i=0;i<m1;i++){
        for(int j=0;j<n1;j++){
          b[i][j]=inc.readDouble();
        }
      }

      DataOutputStream outc = new DataOutputStream(connection.getOutputStream());
      //imprimirMat(a, "A"+nodo);
      //imprimirMat(b, "B"+nodo);
      //calculando cn
      c = multMat(b,a);
      //imprimirMat(c, "C"+nodo);
      //enviar respuesta
      outc.writeInt(nodo);
      //enviar resultado Cn
      outc.writeInt(c.length);
      //System.out.println("Enviando longitud "+n1+" desde el nodo "+nodo);
      for(int i=0; i<c.length; i++){
        for(int j=0; j<c.length; j++){
          outc.writeDouble(c[i][j]);
        }
      }
      inc.close();
      outc.close();
      connection.close();
    }


  }

  public static void trasponerMat(double[][] m){
    double x;
    for(int i=0; i<m.length; i++){
      for(int j=0; j<m.length; j++){
        if(i>j){
          x = m[i][j];
          m[i][j] = m[j][i];
          m[j][i] = x;
        }
      }
    }
    return;
  }

  public static void imprimirMat(double[][] m, String name){
    System.out.print("\n  "+name+" = ");
    for(int i=0; i<m.length; i++){
      System.out.print ("\t|");
      for(int j=0; j<m[i].length; j++){
        System.out.print(" "+m[i][j]+"\t");
      }
      System.out.print (" |\n");
    }
    return;
  }

  public static double[][] multMat(double[][] m1, double[][]m2){
    double[][] m = new double[m1.length][m2.length];
    try{
      for (int i = 0; i < m1.length; i++) {
          for (int j = 0; j < m2.length; j++) {
              for (int k = 0; k < m1[0].length; k++) {
                if(m1.length==m1[0].length)
                  m[i][j] += m1[i][k] * m2[j][k];
                else
                  m[j][i] += m1[i][k] * m2[j][k];
              }
          }
        }
    }catch(Exception e){
      System.out.println("Error de multiplicacion");
   }
    return m;
  }

  public static double checksum(double[][] m){
    double sum = 0;
    for(int i=0; i<m.length; i++){
      for(int j=0; j<m[0].length; j++){
        sum+=m[i][j];
      }
    }
    return sum;
  }

}
