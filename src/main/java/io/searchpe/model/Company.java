package io.searchpe.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "company", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"version", "ruc"})
})
@NamedQueries(value = {
        @NamedQuery(name = "getCompaniesByVersionIdAndRuc", query = "select c from Company c inner join c.version v where v.id=:versionId and c.ruc=:ruc"),
        @NamedQuery(name = "getCompaniesByVersionIdAndRazonSocial", query = "select c from Company c inner join c.version v where v.id=:versionId and lower(c.razonSocial) like :razonSocial"),
        @NamedQuery(name = "getCompaniesByVersionIdAndFilterText", query = "select c from Company c inner join c.version v where v.id=:versionId and ( lower(c.ruc) like :filterText or lower(c.razonSocial) like :filterText)"),
        @NamedQuery(name = "deleteCompaniesByVersionId", query = "delete from Company c where c.version.id=:versionId")
})
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "company-pooled-lo")
    @GenericGenerator(name = "company-pooled-lo", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
            @org.hibernate.annotations.Parameter(name = "sequence_name", value = "sequence"),
            @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
            @org.hibernate.annotations.Parameter(name = "increment_size", value = "50"),
            @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled-lo")
    })
    private Long id;

    @NotNull
    @Column(name = "ruc")
    private String ruc;

    @NotNull
    @Column(name = "razon_social")
    private String razonSocial;

    @NotNull
    @Column(name = "estado_contribuyente")
    private String estadoContribuyente;

    @NotNull
    @Column(name = "condicion_domicilio")
    private String condicionDomicilio;

    @NotNull
    @Column(name = "ubigeo")
    private String ubigeo;

    @NotNull
    @Column(name = "tipo_via")
    private String tipoVia;

    @NotNull
    @Column(name = "nombre_via")
    private String nombreVia;

    @NotNull
    @Column(name = "codigo_zona")
    private String codigoZona;

    @NotNull
    @Column(name = "tipo_zona")
    private String tipoZona;

    @NotNull
    @Column(name = "numero")
    private String numero;

    @NotNull
    @Column(name = "interior")
    private String interior;

    @NotNull
    @Column(name = "lote")
    private String lote;

    @NotNull
    @Column(name = "departamento")
    private String departamento;

    @NotNull
    @Column(name = "manzana")
    private String manzana;

    @NotNull
    @Column(name = "kilometro")
    private String kilometro;

    @Transient
    private String otro;

    @JsonIgnore
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version", foreignKey = @ForeignKey)
    private io.searchpe.model.Version version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getEstadoContribuyente() {
        return estadoContribuyente;
    }

    public void setEstadoContribuyente(String estadoContribuyente) {
        this.estadoContribuyente = estadoContribuyente;
    }

    public String getCondicionDomicilio() {
        return condicionDomicilio;
    }

    public void setCondicionDomicilio(String condicionDomicilio) {
        this.condicionDomicilio = condicionDomicilio;
    }

    public String getUbigeo() {
        return ubigeo;
    }

    public void setUbigeo(String ubigeo) {
        this.ubigeo = ubigeo;
    }

    public String getTipoVia() {
        return tipoVia;
    }

    public void setTipoVia(String tipoVia) {
        this.tipoVia = tipoVia;
    }

    public String getNombreVia() {
        return nombreVia;
    }

    public void setNombreVia(String nombreVia) {
        this.nombreVia = nombreVia;
    }

    public String getCodigoZona() {
        return codigoZona;
    }

    public void setCodigoZona(String codigoZona) {
        this.codigoZona = codigoZona;
    }

    public String getTipoZona() {
        return tipoZona;
    }

    public void setTipoZona(String tipoZona) {
        this.tipoZona = tipoZona;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getInterior() {
        return interior;
    }

    public void setInterior(String interior) {
        this.interior = interior;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getManzana() {
        return manzana;
    }

    public void setManzana(String manzana) {
        this.manzana = manzana;
    }

    public String getKilometro() {
        return kilometro;
    }

    public void setKilometro(String kilometro) {
        this.kilometro = kilometro;
    }

    public io.searchpe.model.Version getVersion() {
        return version;
    }

    public void setVersion(io.searchpe.model.Version version) {
        this.version = version;
    }

    public String getOtro() {
        return otro;
    }

    public void setOtro(String otro) {
        this.otro = otro;
    }
}
