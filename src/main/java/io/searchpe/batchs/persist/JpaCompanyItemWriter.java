package io.searchpe.batchs.persist;

import io.searchpe.model.Company;
import io.searchpe.model.Version;
import io.searchpe.services.VersionService;
import org.jberet.support.io.JpaItemWriter;

import javax.batch.api.BatchProperty;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
public class JpaCompanyItemWriter extends JpaItemWriter {

    @Inject
    @BatchProperty
    protected String versionId;

    @Inject
    private VersionService versionService;

    @Override
    public void writeItems(List<Object> items) throws Exception {
        try {


            if (entityTransaction) {
                em.getTransaction().begin();
            }

            Version version = versionService.getVersion(versionId)
                    .orElseThrow(() -> new IllegalStateException("Version id[" + versionId + "] does not exists"));

            for (final Object e : items) {
                Company company = (Company) e;
                company.setVersion(version);

//            System.out.println(
//                    "ruc:" + company.getRuc() + "(" + company.getRuc().length() + ")" +
//                            " razon_social:" + company.getRazonSocial() + "(" + company.getRazonSocial().length() + ")" +
//                            " estado_contribuyente:" + company.getEstadoContribuyente() + "(" + company.getEstadoContribuyente().length() + ")" +
//                            " condicion_domicilio:" + company.getCondicionDomicilio() + "(" + company.getCondicionDomicilio().length() + ")" +
//                            " ubigeo:" + company.getUbigeo() + "(" + company.getUbigeo().length() + ")" +
//                            " tipo_via:" + company.getTipoVia() + "(" + company.getTipoVia().length() + ")" +
//                            " nombre_via:" + company.getNombreVia() + "(" + company.getNombreVia().length() + ")" +
//                            " codigo_zona:" + company.getCodigoZona() + "(" + company.getCodigoZona().length() + ")" +
//                            " tipo_zona:" + company.getTipoZona() + "(" + company.getTipoZona().length() + ")" +
//                            " numero:" + company.getNumero() + "(" + company.getNumero().length() + ")" +
//                            " interior:" + company.getInterior() + "(" + company.getInterior().length() + ")" +
//                            " lote:" + company.getLote() + "(" + company.getLote().length() + ")" +
//                            " departamento:" + company.getDepartamento() + "(" + company.getDepartamento().length() + ")" +
//                            " manzana:" + company.getManzana() + "(" + company.getManzana().length() + ")" +
//                            " kilometro:" + company.getKilometro() + "(" + company.getKilometro().length() + ")"
//            );

                em.persist(e);
//            em.flush();
            }

            if (entityTransaction) {
                em.getTransaction().commit();
            }

        } catch (Throwable e) {
            System.out.println("holaaaa");
            System.exit(0);
        }
    }

}
