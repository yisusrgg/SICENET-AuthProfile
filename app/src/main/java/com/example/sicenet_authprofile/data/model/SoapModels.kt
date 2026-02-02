package com.example.sicenet_authprofile.data.model

import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.NamespaceList
import org.simpleframework.xml.Root

@Root(name = "Envelope", strict = false)
@NamespaceList(
    Namespace(prefix = "soap", reference = "http://schemas.xmlsoap.org/soap/envelope/")
)
data class SoapEnvelope(
    @field:Element(name = "Body", required = false)
    @param:Element(name = "Body", required = false)
    var body: SoapBody? = null
)

@Root(name = "Body", strict = false)
data class SoapBody(
    @field:Element(name = "accesoLoginResponse", required = false)
    @param:Element(name = "accesoLoginResponse", required = false)
    var loginResponse: AccesoLoginResponse? = null,

    @field:Element(name = "getAlumnoAcademicoWithLineamientoResponse", required = false)
    @param:Element(name = "getAlumnoAcademicoWithLineamientoResponse", required = false)
    var profileResponse: ProfileResponse? = null
)

@Root(name = "accesoLoginResponse", strict = false)
data class AccesoLoginResponse(
    @field:Element(name = "accesoLoginResult", required = false)
    @param:Element(name = "accesoLoginResult", required = false)
    var result: String? = null
)

@Root(name = "getAlumnoAcademicoWithLineamientoResponse", strict = false)
data class ProfileResponse(
    @field:Element(name = "getAlumnoAcademicoWithLineamientoResult", required = false)
    @param:Element(name = "getAlumnoAcademicoWithLineamientoResult", required = false)
    var result: String? = null
)
