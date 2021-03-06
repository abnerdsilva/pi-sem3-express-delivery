package delivery.model.dao;

public class PedidoItemDao {
    private int codPedidoItem;
    private int codPedido;
    private int codProduto;
    private int quantidade;
    private double vrUnitario;
    private double vrTotal;
    private String observacao;

    public PedidoItemDao() {
    }

    public int getCodPedidoItem() {
        return codPedidoItem;
    }

    public void setCodPedidoItem(int codPedidoItem) {
        this.codPedidoItem = codPedidoItem;
    }

    public int getCodPedido() {
        return codPedido;
    }

    public void setCodPedido(int codPedido) {
        this.codPedido = codPedido;
    }

    public int getCodProduto() {
        return codProduto;
    }

    public void setCodProduto(int codProduto) {
        this.codProduto = codProduto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public double getVrUnitario() {
        return vrUnitario;
    }

    public void setVrUnitario(double vrUnitario) {
        this.vrUnitario = vrUnitario;
    }

    public double getVrTotal() {
        return vrTotal;
    }

    public void setVrTotal(double vrTotal) {
        this.vrTotal = vrTotal;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
