package ifood.controller;

import delivery.controller.PedidoController;
import delivery.model.ClienteDelivery;
import delivery.model.PagamentoDelivery;
import delivery.model.PedidoDelivery;
import delivery.model.PedidoItemDelivery;
import ifood.model.OrderIntegration;
import ifood.model.dao.PedidoDao;
import ifood.repository.OrderRepository;
import log.LoggerInFile;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Order {

    private final static OrderRepository repository = new OrderRepository();

    public static void saveOrdersPending() {
        try {
            List<OrderIntegration> ordersPending = repository.getOrdersPendingToConfirmation();

            if (ordersPending.size() > 0) {
                for (OrderIntegration order : ordersPending) {
                    ifood.model.Order orderDetails = repository.getOrderDetails(order.getCodPedidoIntegracao());

                    PedidoDelivery pedido = trataPedido(orderDetails, order);

                    try {
                        PedidoController pedidoController = new PedidoController();
                        int orderId = pedidoController.savePedido(pedido);
                        if (repository.updateOrderId(orderId, pedido.getCodPedidoIntegracao())) {
                            LoggerInFile.printInfo("Sucesso");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        LoggerInFile.printError(e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            LoggerInFile.printError(e.getMessage());
        }
    }

    public static void ordersToConfirmProduction() {
        List<PedidoDao> confirmedOrders = new ArrayList<>();
        for (; ; ) {
            try {
                List<PedidoDao> ordersPending = repository.getOrdersToConfirmProduction();
                for (PedidoDao orderPending : confirmedOrders) {
                    var statusPedidoConfirmar = false;
                    for (PedidoDao order : ordersPending) {
//                        System.out.println(orderPending.toString());
                        if (orderPending.getCodPedido() == order.getCodPedido()) {
                            statusPedidoConfirmar = true;
                        }
                    }

                    if (!statusPedidoConfirmar) {
                        System.out.println("confirmar pedido -> " + orderPending.toString());
                        if (repository.confirmProductionOrder(orderPending.getCodPedidoIntegracao())) {
                            LoggerInFile.printInfo("Pedido confirmado com sucesso");
                        }
                    }
                }

                confirmedOrders = ordersPending;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void ordersToConfirmDispatch() {
        List<PedidoDao> dispatchOrders = new ArrayList<>();
        for (; ; ) {
            try {
                List<PedidoDao> ordersPending = repository.getOrdersToDispatch();
                for (PedidoDao orderPending : dispatchOrders) {
                    var statusPedidoDispatch = false;
                    for (PedidoDao order : ordersPending) {
//                        System.out.println(orderPending.toString());
                        if (orderPending.getCodPedido() == order.getCodPedido()) {
                            statusPedidoDispatch = true;
                        }
                    }

                    if (!statusPedidoDispatch) {
                        System.out.println("despacha pedido -> " + orderPending.toString());
                        if (repository.confirmDispatchOrder(orderPending.getCodPedidoIntegracao())) {
                            LoggerInFile.printInfo("Pedido despachado com sucesso");
                        }
                    }
                }

                dispatchOrders = ordersPending;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static PedidoDelivery trataPedido(ifood.model.Order order, OrderIntegration orderIntegration) {
        PagamentoDelivery pagamentoDelivery = new PagamentoDelivery();
        pagamentoDelivery.setNome(order.getPayments().getMethods().get(0).getCard().getBrand());
        pagamentoDelivery.setPrePago(order.getPayments().getMethods().get(0).isPrepaid());
        pagamentoDelivery.setTipo(order.getPayments().getMethods().get(0).getMethod());
        pagamentoDelivery.setTroco(order.getPayments().getPending());
        pagamentoDelivery.setValor(order.getPayments().getPrepaid());

        List<PedidoItemDelivery> itens = new ArrayList<>();

        for (ifood.model.Order.OrderItems item : order.getItems()) {
            PedidoItemDelivery pedidoItemDelivery = new PedidoItemDelivery();
            pedidoItemDelivery.setNome(item.getName());
            pedidoItemDelivery.setCodExterno(item.getExternalCode());
            String obs = "";
            for (ifood.model.Order.OrderItems.OrderItemsOptions option : item.getOptions()) {
                obs += " " + option.getName() + "\n";
            }
            obs += " " + item.getObservations() + "\n";
            pedidoItemDelivery.setObservacao(obs);
            pedidoItemDelivery.setQuantidade(item.getQuantity());
            pedidoItemDelivery.setVrDesconto(0);
            pedidoItemDelivery.setVrAdicional(item.getOptionsPrice());
            pedidoItemDelivery.setVrUnit(item.getUnitPrice());
            pedidoItemDelivery.setVrTotal(item.getTotalPrice());

            itens.add(pedidoItemDelivery);
        }

        ClienteDelivery clienteDelivery = new ClienteDelivery();
        clienteDelivery.setNome(order.getCustomer().getName());
        clienteDelivery.setCodCliente(1006);
        clienteDelivery.setDocumento(order.getCustomer().getDocumentNumber());
        clienteDelivery.setBairro(order.getDelivery().getDeliveryAddress().getNeighborhood());
        clienteDelivery.setLogradouro(order.getDelivery().getDeliveryAddress().getStreetName());
        int numero = 0;
        if (order.getDelivery().getDeliveryAddress().getStreeNumber() != null) {
            numero = Integer.parseInt(order.getDelivery().getDeliveryAddress().getStreeNumber());
        }
        clienteDelivery.setNumero(numero);
        clienteDelivery.setCidade(order.getDelivery().getDeliveryAddress().getCity());
        clienteDelivery.setEstado(order.getDelivery().getDeliveryAddress().getState());
        clienteDelivery.setCep(Integer.parseInt(order.getDelivery().getDeliveryAddress().getPostalCode()));
        clienteDelivery.setEmail("");
        clienteDelivery.setTelefone(order.getDisplayId());

//        LocalTime lt1 = LocalTime.parse(order.getCreatedAt()).minusHours(3);

        PedidoDelivery pedidoDelivery = new PedidoDelivery();
        pedidoDelivery.setCodPedido(0);
        pedidoDelivery.setDataCriacao(order.getCreatedAt());
//        pedidoDelivery.setDataCriacao(lt1.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));
        pedidoDelivery.setAgendado(!order.getOrderTiming().equals("IMMEDIATE"));
        pedidoDelivery.setDataEntrega(order.getDelivery().getDeliveryDateTime());
        pedidoDelivery.setObservacao(order.getDelivery().getObservations());
        pedidoDelivery.setReferencia(order.getDelivery().getDeliveryAddress().getReference());
        pedidoDelivery.setReferenciaCurta(order.getDisplayId());
        pedidoDelivery.setTipo(order.getOrderType());
        pedidoDelivery.setVrTotal(order.getTotal().getSubTotal());
        pedidoDelivery.setVrAdicional(order.getTotal().getDeliveryFee() + order.getTotal().getAdditionalFees());
        pedidoDelivery.setVrDesconto(order.getTotal().getBenefits());
        pedidoDelivery.setPagamento(pagamentoDelivery);
        pedidoDelivery.setItens(itens);
        pedidoDelivery.setCliente(clienteDelivery);
        pedidoDelivery.setOrigem(order.getSalesChannel());
        pedidoDelivery.setCodPedidoIntegracao(orderIntegration.getCodPedidoIntegracao());

        return pedidoDelivery;
    }
}
