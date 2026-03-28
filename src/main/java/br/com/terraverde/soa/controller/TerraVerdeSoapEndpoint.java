package br.com.terraverde.soa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import br.com.terraverde.soa.model.Endereco;
import br.com.terraverde.soa.model.Parceiro;
import br.com.terraverde.soa.model.Produto;
import br.com.terraverde.soa.service.AuthService;
import br.com.terraverde.soa.service.EnderecoService;
import br.com.terraverde.soa.service.ParceiroService;
import br.com.terraverde.soa.service.ProdutoService;
import br.com.terraverde.soa.service.RelatorioService;
import br.com.terraverde.soap.*;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Endpoint
public class TerraVerdeSoapEndpoint {

    private static final String NAMESPACE_URI = "http://terraverde.com.br/soap";

    @Autowired private ParceiroService  parceiroService;
    @Autowired private AuthService      authService;
    @Autowired private RelatorioService relatorioService;
    @Autowired private ProdutoService   produtoService;
    @Autowired private EnderecoService  enderecoService;

    // =========================================================================
    // SERVIÇO 1 — AUTENTICAÇÃO
    // =========================================================================

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "cadastrarUsuarioRequest")
    @ResponsePayload
    public CadastrarUsuarioResponse cadastrarUsuario(@RequestPayload CadastrarUsuarioRequest request) {
        CadastrarUsuarioResponse res = new CadastrarUsuarioResponse();
        try {
            authService.registrarUsuario(request.getUsername(), request.getPassword());
            res.setSucesso(true);
            res.setMensagem("Usuário cadastrado com sucesso!");
        } catch (Exception e) {
            res.setSucesso(false);
            res.setMensagem("Erro ao cadastrar usuário: " + e.getMessage());
        }
        return res;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "loginRequest")
    @ResponsePayload
    public LoginResponse login(@RequestPayload LoginRequest request) {
        LoginResponse res = new LoginResponse();
        try {
            String token = authService.login(request.getUsername(), request.getPassword());
            res.setSucesso(true);
            res.setMensagem(token);
        } catch (Exception e) {
            res.setSucesso(false);
            res.setMensagem(e.getMessage());
        }
        return res;
    }

    // =========================================================================
    // SERVIÇO 2 — PARCEIROS
    // =========================================================================

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "cadastrarParceiroRequest")
    @ResponsePayload
    public CadastrarParceiroResponse cadastrarParceiro(@RequestPayload CadastrarParceiroRequest request) {
        CadastrarParceiroResponse res = new CadastrarParceiroResponse();
        try {
            Parceiro p = new Parceiro();
            p.setNomeOuRazaoSocial(request.getNomeOuRazaoSocial());
            p.setDocumento(request.getDocumento());
            p.setTipo(request.getTipo());
            p.setEmail(request.getEmail());
            p.setTelefone(request.getTelefone());
            p = parceiroService.criar(p);
            res.setId(p.getId());
            res.setMensagem("Parceiro cadastrado com sucesso.");
        } catch (Exception e) {
            res.setId(0L);
            res.setMensagem("Erro: " + e.getMessage());
        }
        return res;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "listarParceirosRequest")
    @ResponsePayload
    public ListarParceirosResponse listarParceiros(@RequestPayload ListarParceirosRequest request) {
        ListarParceirosResponse res = new ListarParceirosResponse();
        try {
            List<ParceiroData> dataList = parceiroService.listar(request.getTipo())
                .stream().map(p -> {
                    ParceiroData pd = new ParceiroData();
                    pd.setId(p.getId());
                    pd.setNomeOuRazaoSocial(p.getNomeOuRazaoSocial());
                    pd.setDocumento(p.getDocumento());
                    pd.setTipo(p.getTipo());
                    pd.setEmail(p.getEmail());
                    pd.setTelefone(p.getTelefone());
                    return pd;
                }).collect(Collectors.toList());
            res.getParceiros().addAll(dataList);
        } catch (Exception e) {
            // Retorna lista vazia em caso de erro
        }
        return res;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "atualizarParceiroRequest")
    @ResponsePayload
    public AtualizarParceiroResponse atualizarParceiro(@RequestPayload AtualizarParceiroRequest request) {
        AtualizarParceiroResponse res = new AtualizarParceiroResponse();
        try {
            Parceiro p = new Parceiro();
            p.setId(request.getId());
            p.setNomeOuRazaoSocial(request.getNomeOuRazaoSocial());
            p.setDocumento(request.getDocumento());
            p.setTipo(request.getTipo());
            p.setEmail(request.getEmail());
            p.setTelefone(request.getTelefone());
            parceiroService.atualizar(p);
            res.setSucesso(true);
            res.setMensagem("Parceiro atualizado com sucesso.");
        } catch (Exception e) {
            res.setSucesso(false);
            res.setMensagem("Erro ao atualizar: " + e.getMessage());
        }
        return res;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "deletarParceiroRequest")
    @ResponsePayload
    public DeletarParceiroResponse deletarParceiro(@RequestPayload DeletarParceiroRequest request) {
        DeletarParceiroResponse res = new DeletarParceiroResponse();
        try {
            parceiroService.remover(request.getId());
            res.setSucesso(true);
            res.setMensagem("Parceiro removido com sucesso.");
        } catch (Exception e) {
            res.setSucesso(false);
            res.setMensagem("Erro ao remover: " + e.getMessage());
        }
        return res;
    }

    // =========================================================================
    // SERVIÇO 3 — RELATÓRIO
    // =========================================================================

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "gerarRelatorioPdfRequest")
    @ResponsePayload
    public GerarRelatorioPdfResponse gerarRelatorio(@RequestPayload GerarRelatorioPdfRequest request) {
        GerarRelatorioPdfResponse res = new GerarRelatorioPdfResponse();
        try {
            byte[] pdf = relatorioService.gerarRelatorioPdf();
            res.setPdfBase64(Base64.getEncoder().encodeToString(pdf));
            res.setNomeArquivo("relatorio_parceiros.pdf");
        } catch (Exception e) {
            res.setNomeArquivo("erro.txt");
        }
        return res;
    }

    // =========================================================================
    // SERVIÇO 4 — PRODUTOS
    // =========================================================================

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "cadastrarProdutoRequest")
    @ResponsePayload
    public CadastrarProdutoResponse cadastrarProduto(@RequestPayload CadastrarProdutoRequest request) {
        CadastrarProdutoResponse res = new CadastrarProdutoResponse();
        try {
            Produto p = new Produto();
            p.setNome(request.getNome());
            p.setDescricao(request.getDescricao());
            p.setPreco(request.getPreco());
            p.setUnidadeMedida(request.getUnidadeMedida());
            p = produtoService.cadastrar(p);
            res.setId(p.getId());
            res.setMensagem("Produto cadastrado com sucesso.");
        } catch (Exception e) {
            res.setId(0L);
            res.setMensagem("Erro: " + e.getMessage());
        }
        return res;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "listarProdutosRequest")
    @ResponsePayload
    public ListarProdutosResponse listarProdutos(@RequestPayload ListarProdutosRequest request) {
        ListarProdutosResponse res = new ListarProdutosResponse();
        try {
            List<ProdutoData> dataList = produtoService.listar()
                .stream().map(p -> {
                    ProdutoData pd = new ProdutoData();
                    pd.setId(p.getId());
                    pd.setNome(p.getNome());
                    pd.setDescricao(p.getDescricao());
                    pd.setPreco(p.getPreco());
                    pd.setUnidadeMedida(p.getUnidadeMedida());
                    return pd;
                }).collect(Collectors.toList());
            res.getProdutos().addAll(dataList);
        } catch (Exception e) {
            // Retorna lista vazia em caso de erro
        }
        return res;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "atualizarProdutoRequest")
    @ResponsePayload
    public AtualizarProdutoResponse atualizarProduto(@RequestPayload AtualizarProdutoRequest request) {
        AtualizarProdutoResponse res = new AtualizarProdutoResponse();
        try {
            Produto p = new Produto();
            p.setId(request.getId());
            p.setNome(request.getNome());
            p.setDescricao(request.getDescricao());
            p.setPreco(request.getPreco());
            p.setUnidadeMedida(request.getUnidadeMedida());
            produtoService.atualizar(p);
            res.setSucesso(true);
            res.setMensagem("Produto atualizado com sucesso.");
        } catch (Exception e) {
            res.setSucesso(false);
            res.setMensagem("Erro ao atualizar: " + e.getMessage());
        }
        return res;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "deletarProdutoRequest")
    @ResponsePayload
    public DeletarProdutoResponse deletarProduto(@RequestPayload DeletarProdutoRequest request) {
        DeletarProdutoResponse res = new DeletarProdutoResponse();
        try {
            produtoService.remover(request.getId());
            res.setSucesso(true);
            res.setMensagem("Produto removido com sucesso.");
        } catch (Exception e) {
            res.setSucesso(false);
            res.setMensagem("Erro ao remover: " + e.getMessage());
        }
        return res;
    }

    // =========================================================================
    // SERVIÇO 5 — ENDEREÇOS DE PARCEIROS
    // =========================================================================

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "cadastrarEnderecoRequest")
    @ResponsePayload
    public CadastrarEnderecoResponse cadastrarEndereco(@RequestPayload CadastrarEnderecoRequest request) {
        CadastrarEnderecoResponse res = new CadastrarEnderecoResponse();
        try {
            Endereco e = new Endereco();
            e.setLogradouro(request.getLogradouro());
            e.setNumero(request.getNumero());
            e.setComplemento(request.getComplemento());
            e.setBairro(request.getBairro());
            e.setCidade(request.getCidade());
            e.setEstado(request.getEstado());
            e.setCep(request.getCep());
            e = enderecoService.cadastrar(request.getParceiroId(), e);
            res.setId(e.getId());
            res.setMensagem("Endereço cadastrado com sucesso.");
        } catch (Exception ex) {
            res.setId(0L);
            res.setMensagem("Erro: " + ex.getMessage());
        }
        return res;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "listarEnderecosRequest")
    @ResponsePayload
    public ListarEnderecosResponse listarEnderecos(@RequestPayload ListarEnderecosRequest request) {
        ListarEnderecosResponse res = new ListarEnderecosResponse();
        try {
            List<EnderecoData> dataList = enderecoService.listarPorParceiro(request.getParceiroId())
                .stream().map(e -> {
                    EnderecoData ed = new EnderecoData();
                    ed.setId(e.getId());
                    ed.setParceiroId(e.getParceiro().getId());
                    ed.setLogradouro(e.getLogradouro());
                    ed.setNumero(e.getNumero());
                    ed.setComplemento(e.getComplemento());
                    ed.setBairro(e.getBairro());
                    ed.setCidade(e.getCidade());
                    ed.setEstado(e.getEstado());
                    ed.setCep(e.getCep());
                    return ed;
                }).collect(Collectors.toList());
            res.getEnderecos().addAll(dataList);
        } catch (Exception e) {
            // Retorna lista vazia em caso de erro
        }
        return res;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "atualizarEnderecoRequest")
    @ResponsePayload
    public AtualizarEnderecoResponse atualizarEndereco(@RequestPayload AtualizarEnderecoRequest request) {
        AtualizarEnderecoResponse res = new AtualizarEnderecoResponse();
        try {
            Endereco e = new Endereco();
            e.setId(request.getId());
            e.setLogradouro(request.getLogradouro());
            e.setNumero(request.getNumero());
            e.setComplemento(request.getComplemento());
            e.setBairro(request.getBairro());
            e.setCidade(request.getCidade());
            e.setEstado(request.getEstado());
            e.setCep(request.getCep());
            enderecoService.atualizar(e);
            res.setSucesso(true);
            res.setMensagem("Endereço atualizado com sucesso.");
        } catch (Exception ex) {
            res.setSucesso(false);
            res.setMensagem("Erro ao atualizar: " + ex.getMessage());
        }
        return res;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "removerEnderecoRequest")
    @ResponsePayload
    public RemoverEnderecoResponse removerEndereco(@RequestPayload RemoverEnderecoRequest request) {
        RemoverEnderecoResponse res = new RemoverEnderecoResponse();
        try {
            enderecoService.remover(request.getId());
            res.setSucesso(true);
            res.setMensagem("Endereço removido com sucesso.");
        } catch (Exception e) {
            res.setSucesso(false);
            res.setMensagem("Erro ao remover: " + e.getMessage());
        }
        return res;
    }
}