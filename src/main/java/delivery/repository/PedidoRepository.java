package delivery.repository;

import db.DatabaseConnection;
import delivery.model.dao.PedidoDao;
import delivery.model.dao.PedidoItemDao;
import delivery.repository.interfaces.IPedidoRepository;
import log.LoggerInFile;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PedidoRepository implements IPedidoRepository {
    /**
     * consulta itens do pedido de acordo com codigo do pedido
     *
     * @param code - codigo do pedido que será consultado
     * @return - retorna lista com itens do pedido
     */
    @Override
    public List<PedidoItemDao> loadItensByCode(int code) {
        String sql = "SELECT * FROM TB_PEDIDO_ITEM WHERE COD_PEDIDO = " + code;

        List<PedidoItemDao> itens = new ArrayList<>();

        DatabaseConnection bd = new DatabaseConnection();
        bd.getConnection();

        try {
            bd.st = bd.connection.prepareStatement(sql);
            bd.rs = bd.st.executeQuery();
            while (bd.rs.next()) {
                PedidoItemDao item = new PedidoItemDao();
                item.setCodPedido(bd.rs.getInt("cod_pedido"));
                item.setCodPedidoItem(bd.rs.getInt("cod_pedido_item"));
                item.setCodProduto(bd.rs.getInt("cod_produto"));
                item.setObservacao(bd.rs.getString("observacao"));
                item.setQuantidade(bd.rs.getInt("quantidade"));
                item.setVrTotal(bd.rs.getDouble("vr_total"));
                item.setVrUnitario(bd.rs.getDouble("vr_unitario"));

                itens.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LoggerInFile.printError(e.getMessage());
        } finally {
            bd.close();
        }

        return itens;
    }

    /**
     * consulta codigo do ultimo pedido cadastrado
     *
     * @return - retorna codigo do ultimo pedido ou -1 quando ocorre erro
     */
    @Override
    public int loadMaxOrder() {
        String sql = "SELECT MAX(COD_PEDIDO) AS orderID FROM TB_PEDIDO";

        DatabaseConnection bd = new DatabaseConnection();
        bd.getConnection();

        try {
            bd.st = bd.connection.prepareStatement(sql);
            bd.rs = bd.st.executeQuery();
            if (bd.rs.next()) {
                return bd.rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LoggerInFile.printError(e.getMessage());
        } finally {
            bd.close();
        }

        return -1;
    }

    /**
     * salva dados do pedido
     *
     * @param pedido - informações do pedido
     * @return - retorna id do pedido cadastado ou -1 quando ocorre erro
     */
    @Override
    public int saveOrder(PedidoDao pedido) {
        String sql = "INSERT INTO TB_PEDIDO (COD_CLIENTE, DATA_PEDIDO, DATA_ENTREGA, VR_TOTAL, VR_TAXA, VR_TROCO, LOGRADOURO, NUMERO, BAIRRO, CIDADE, ESTADO, CEP, TIPO_PEDIDO, ORIGEM, OBSERVACAO, FORMA_PAGAMENTO, cod_pedido_integracao) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        DatabaseConnection bd = new DatabaseConnection();
        bd.getConnection();
        try {
            bd.connection.beginRequest();
            bd.st = bd.connection.prepareStatement(sql);
            bd.st.setInt(1, pedido.getCodCliente());
            bd.st.setString(2, pedido.getDataPedido());
            bd.st.setString(3, pedido.getDataEntrega());
            bd.st.setDouble(4, pedido.getVrTotal());
            bd.st.setDouble(5, pedido.getVrTaxa());
            bd.st.setDouble(6, pedido.getVrTroco());
            bd.st.setString(7, pedido.getLogradouro());
            bd.st.setInt(8, pedido.getNumero());
            bd.st.setString(9, pedido.getBairro());
            bd.st.setString(10, pedido.getCidade());
            bd.st.setString(11, pedido.getEstado());
            bd.st.setString(12, pedido.getCep());
            bd.st.setString(13, pedido.getTipoPedido());
            bd.st.setString(14, pedido.getOrigem());
            bd.st.setString(15, pedido.getObservacao());
            bd.st.setString(16, pedido.getFormaPagamento());
            bd.st.setString(17, pedido.getCodPedidoIntegracao());
            int resultInsert = bd.st.executeUpdate();
            if (resultInsert > 0) {
                int maxOrderId = loadMaxOrder();
                if (maxOrderId > 0) {
                    return maxOrderId;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LoggerInFile.printError(e.getMessage());
        } finally {
            bd.close();
        }

        return -1;
    }

    /**
     * salva item do pedido
     *
     * @param item - informações do item
     * @return - retorna id do item do pedido que foi salvo ou -1 quando ocorre erro
     */
    @Override
    public int saveOrderItem(PedidoItemDao item) {
        String sql = "INSERT INTO TB_PEDIDO_ITEM (COD_PEDIDO, COD_PRODUTO, QUANTIDADE, VR_UNITARIO, VR_TOTAL, OBSERVACAO) VALUES (?,?,?,?,?,?)";

        DatabaseConnection bd = new DatabaseConnection();
        bd.getConnection();

        try {
            bd.st = bd.connection.prepareStatement(sql);
            bd.st.setInt(1, item.getCodPedido());
            bd.st.setInt(2, item.getCodProduto());
            bd.st.setInt(3, item.getQuantidade());
            bd.st.setDouble(4, item.getVrUnitario());
            bd.st.setDouble(5, item.getVrTotal());
            bd.st.setString(6, item.getObservacao());

            return bd.st.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            LoggerInFile.printError(e.getMessage());
        } finally {
            bd.close();
        }

        return -1;
    }

    /**
     * atualiza status de impresso o pedido para 0
     *
     * @param idPedido - id do pedido que será atualizado
     * @return - retorna 1 para atualizado e -1 quando ocorre erro
     */
    @Override
    public int updateOrderPrinted(int idPedido) {
        int ret = -1;

        String insertSql = "UPDATE TB_PEDIDO SET IMPRIME_PEDIDO=0 WHERE COD_PEDIDO = ?";

        DatabaseConnection bd = new DatabaseConnection();
        bd.getConnection();

        try {
            PreparedStatement prepsInsertProduct = bd.connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            prepsInsertProduct.setInt(1, idPedido);
            prepsInsertProduct.execute();
            bd.rs = prepsInsertProduct.getGeneratedKeys();

            if (bd.rs.next()) {
                return 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LoggerInFile.printError(e.getMessage());
        } finally {
            bd.close();
        }

        return ret;
    }

    /**
     * consulta pedidos que estão com status de imprime_pedido = 1
     *
     * @return - retorna lista com pedidos pendentes para imprimir
     */
    @Override
    public List<PedidoDao> getPedidosParaImprimir() {
        String sql = "SELECT * FROM TB_PEDIDO WHERE IMPRIME_PEDIDO = 1";

        List<PedidoDao> pedidos = new ArrayList<>();

        DatabaseConnection bd = new DatabaseConnection();
        bd.getConnection();

        try {
            bd.st = bd.connection.prepareStatement(sql);
            bd.rs = bd.st.executeQuery();
            while (bd.rs.next()) {
                PedidoDao pedido = new PedidoDao();
                pedido.setCodPedido(bd.rs.getInt("cod_pedido"));
                pedido.setCodPedidoIntegracao(bd.rs.getString("cod_pedido_integracao"));
                pedido.setDataEntrega(bd.rs.getString("data_entrega"));
                pedido.setDataPedido(bd.rs.getString("data_pedido"));
                pedido.setLogradouro(bd.rs.getString("logradouro"));
                pedido.setNumero(bd.rs.getInt("numero"));
                pedido.setBairro(bd.rs.getString("bairro"));
                pedido.setCidade(bd.rs.getString("cidade"));
                pedido.setEstado(bd.rs.getString("estado"));
                pedido.setCep(bd.rs.getString("cep"));
                pedido.setTipoPedido(bd.rs.getString("tipo_pedido"));
                pedido.setOrigem(bd.rs.getString("origem"));
                pedido.setObservacao(bd.rs.getString("observacao"));
                pedido.setDataAtualizacao(bd.rs.getString("data_atualizacao"));
                pedido.setFormaPagamento(bd.rs.getString("forma_pagamento"));
                pedido.setCodCliente(bd.rs.getInt("cod_cliente"));
                pedido.setVrTotal(bd.rs.getDouble("vr_total"));
                pedido.setVrTaxa(bd.rs.getDouble("vr_taxa"));
                pedido.setVrTroco(bd.rs.getDouble("vr_troco"));

                pedidos.add(pedido);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LoggerInFile.printError(e.getMessage());
        } finally {
            bd.close();
        }

        return pedidos;
    }
}
