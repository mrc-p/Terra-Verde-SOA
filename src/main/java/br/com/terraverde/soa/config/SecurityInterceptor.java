package br.com.terraverde.soa.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;

import br.com.terraverde.soa.service.TokenService;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.Iterator;

@Component
public class SecurityInterceptor implements EndpointInterceptor {

    @Autowired
    private TokenService tokenService;

    @Override
    public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
        SoapMessage soapMessage = (SoapMessage) messageContext.getRequest();

        // 1. Lê o payload para identificar a operação solicitada
        String payload = "";
        try {
            if (soapMessage.getPayloadSource() != null) {
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer transformer = tf.newTransformer();
                StringWriter writer = new StringWriter();
                transformer.transform(soapMessage.getPayloadSource(), new StreamResult(writer));
                payload = writer.toString();
            }
        } catch (Exception e) {
            // Payload permanece vazio; a validação de token a seguir tratará o caso
        }

        // 2. Operações públicas: não exigem token (login e cadastro de usuário)
        if (payload.contains("loginRequest") ||
        	    payload.contains("LoginRequest") ||
        	    payload.contains("cadastrarUsuarioRequest") ||
        	    payload.contains("listarProdutosRequest")) {
        	    return true;
        	}

        // 3. Operações protegidas: extrai o token do cabeçalho SOAP e valida
        //    via TokenService (sem conhecer como o token foi gerado ou armazenado)
        if (soapMessage.getSoapHeader() != null) {
            Iterator<SoapHeaderElement> it = soapMessage.getSoapHeader().examineAllHeaderElements();
            while (it.hasNext()) {
                SoapHeaderElement header = it.next();
                if (header.getName().getLocalPart().equalsIgnoreCase("token")) {
                    String tokenEnviado = header.getText();
                    if (tokenService.validarToken(tokenEnviado)) {
                        return true;
                    }
                }
            }
        }

        // 4. Token ausente ou inválido: bloqueia a requisição
        throw new RuntimeException("ACESSO NEGADO: Token inválido ou ausente. Faça login primeiro.");
    }

    @Override
    public boolean handleResponse(MessageContext mc, Object e) { return true; }

    @Override
    public boolean handleFault(MessageContext mc, Object e) { return true; }

    @Override
    public void afterCompletion(MessageContext mc, Object e, Exception ex) { }
}