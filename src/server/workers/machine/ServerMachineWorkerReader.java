package server.workers.machine;
import java.io.DataInputStream;
import carrier.Carrier;
import client.machine.Machine;
import packets.JobPacket;
import packets.MachinePacket;
import packets.Packet;
import server.containers.ServerContainer;


public class ServerMachineWorkerReader implements Runnable{

    private String nonce;
    private DataInputStream inputStream;
    private ServerContainer serverContainer;


    public ServerMachineWorkerReader(String nonce, ServerContainer serverContainer, DataInputStream inputStream){
        this.nonce = nonce;
        this.serverContainer = serverContainer;
        this.inputStream = inputStream;
    }


    private void registerHandler(Packet packet){
        Machine machine = ((MachinePacket) packet).getMachine();
        machine.setIdentifier(this.nonce);
        this.serverContainer.addMachine(machine);
    }


    private void jobHandler(Packet packet){
        JobPacket jobPacket = (JobPacket) packet;
        this.serverContainer.finalizeJobPacket(jobPacket);
        this.serverContainer.addResultPacket(jobPacket.getClientNonce(),jobPacket);
    }


    public void run(){

        try{

            Packet packet;
            Carrier carrier = Carrier.getInstance();

            while ((packet = carrier.receivePacket(inputStream)) != null){

                switch (packet.getProtocol()){

                    case MACHINE_INFO:
                        registerHandler(packet);
                        break;

                    case JOB:
                    case ERROR:
                        jobHandler(packet);
                        break;
                
                    default:
                        break;
                }
            }
        }

        catch (Exception e){
            e.printStackTrace();
        }
    }
}