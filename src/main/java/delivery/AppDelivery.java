package delivery;

import delivery.controller.PedidoController;
import delivery.model.ClienteDelivery;
import delivery.model.PagamentoDelivery;
import delivery.model.PedidoDelivery;
import delivery.model.PedidoItemDelivery;
import imprime.Impressora;
import log.LoggerInFile;

import javax.print.PrintException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AppDelivery {
    private static final PedidoController pedidoController = new PedidoController();

    public static void main(String[] args) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        PagamentoDelivery pagamentoDelivery = new PagamentoDelivery();
        pagamentoDelivery.setNome("Visa");
        pagamentoDelivery.setPrePago(false);
        pagamentoDelivery.setTipo("credito");
        pagamentoDelivery.setTroco(0);
        pagamentoDelivery.setValor(15.5);

        List<PedidoItemDelivery> itens = new ArrayList<>();

        PedidoItemDelivery pedidoItemDelivery = new PedidoItemDelivery();
        pedidoItemDelivery.setNome("Esfiha de carne");
        pedidoItemDelivery.setCodExterno("1");
        pedidoItemDelivery.setObservacao("Sem azeitona");
        pedidoItemDelivery.setQuantidade(1);
        pedidoItemDelivery.setVrDesconto(0);
        pedidoItemDelivery.setVrAdicional(0);
        pedidoItemDelivery.setVrUnit(15.5);
        pedidoItemDelivery.setVrTotal(15.5);

        itens.add(pedidoItemDelivery);

        ClienteDelivery clienteDelivery = new ClienteDelivery();
        clienteDelivery.setNome("Abner Silva");
        clienteDelivery.setCodCliente(1006);
        clienteDelivery.setDocumento("");
        clienteDelivery.setBairro("Jardim Xpto");
        clienteDelivery.setLogradouro("Rua Xpto");
        clienteDelivery.setNumero(15);
        clienteDelivery.setCidade("Indaiatuba");
        clienteDelivery.setEstado("SP");
        clienteDelivery.setCep(13345700);
        clienteDelivery.setEmail("silvabner@gmail.com");
        clienteDelivery.setTelefone("(19)995323443");

        PedidoDelivery pedidoDelivery = new PedidoDelivery();
        pedidoDelivery.setCodPedido(0);
        pedidoDelivery.setDataCriacao(now.format(dtf));
        pedidoDelivery.setAgendado(false);
        pedidoDelivery.setDataEntrega("2022/04/22 23:58:00");
        pedidoDelivery.setObservacao("sem observação");
        pedidoDelivery.setReferencia("123");
        pedidoDelivery.setReferenciaCurta("");
        pedidoDelivery.setTipo("ENTREGA");
        pedidoDelivery.setVrTotal(15.5);
        pedidoDelivery.setVrAdicional(0.0);
        pedidoDelivery.setVrDesconto(0.0);
        pedidoDelivery.setPagamento(pagamentoDelivery);
        pedidoDelivery.setItens(itens);
        pedidoDelivery.setCliente(clienteDelivery);
        pedidoDelivery.setOrigem("IFOOD");

        try {
            pedidoController.savePedido(pedidoDelivery);
        } catch (SQLException e) {
            e.printStackTrace();
            LoggerInFile.printError(e.getMessage());
        }

        Impressora imprimir = new Impressora();

        String textToPrint = "\n-----------------------------------------------------------\n";
        textToPrint += imprimir.preencheLinha("Pedido Delivery", " ", 60, "E");
        textToPrint += "\n-----------------------------------------------------------";

        textToPrint += "\n";
        textToPrint += "\n";

        textToPrint += imprimir.preencheLinha("VIA: 1", "*", 60, "ED");
        textToPrint += "\n";
        textToPrint += "\n";

        textToPrint += imprimir.preencheLinha("Nome", " ", 40, "M")
                + imprimir.preencheLinha("Qtde", " ", 7, "M")
                + imprimir.preencheLinha("V.Unit", " ", 7, "M")
                + imprimir.preencheLinha("V.Total", " ", 7, "M");
//                + preencheLinha("1", "*", 6, "EDB")
//                + preencheLinha("1540,54", "*", 10, "EDB") + "\n";
        for (var p : pedidoDelivery.getItens()) {
            textToPrint += imprimir.preencheLinha(p.getNome(), "-", 40, "M");
//                + preencheLinha("1", "*", 6, "EDB")
//                + preencheLinha("1540,54", "*", 10, "EDB") + "\n";
        }


        imprimir.detectaImpressoras();
        try {
            imprimir.imprime(textToPrint);
            imprimir.imprime("\n");
            imprimir.imprime("\n");
        } catch (PrintException e) {
            e.printStackTrace();
            LoggerInFile.printError(e.getMessage());
        }
//        imprimir.imprime2();
    }
}