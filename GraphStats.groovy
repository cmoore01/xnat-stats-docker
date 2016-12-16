import groovyx.net.http.HTTPBuilder
import org.apache.http.impl.cookie.BasicClientCookie
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import java.nio.file.Path

import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

@Grapes([
        @Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1'),
        @Grab(group='org.apache.httpcomponents', module='httpmime', version='4.2.5')
])
def cli = new CliBuilder(usage: 'groovy GraphStats.groovy <parameters>')

cli.with {
    j(longOpt: 'project', 'XNAT project ID to be scraped.', required: true, args: 1)
    s(longOpt: 'url', 'URL for the XNAT server.', required: true, args: 1)
    u(longOpt: 'username', 'Username for XNAT account to access project', required: true, args: 1)
    p(longOpt: 'username', 'Password for XNAT account to access project', required: true, args: 1)
    d(longOpt: 'dependencyMode', 'Resolves dependencies so that they do not require download later.', required: false)
}

def params = cli.parse(args)
if (!params) return
if (params.d) {
    print 'Dependencies resolved.'
    return
}

String url = params.url
String host = new URI(url).getHost()
String project = params.project

HTTPBuilder restClient = new HTTPBuilder(url)
HTTPBuilder sessionGetter = new HTTPBuilder(url)
sessionGetter.headers['Authorization'] = 'Basic ' + "${params.u}:${params.p}".bytes.encodeBase64() // Get a JSESSION using user:pass once, then auth using this. Need to do preemptive or we get an anonymous JSESSION since XNAT is fine with returning one for the anon user.

sessionGetter.request(GET, TEXT) { req ->
    uri.path = '/data/JSESSION'

    response.success = { resp, reader ->
        BasicClientCookie jsessionCookie = new BasicClientCookie("JSESSIONID", reader.text as String)
        jsessionCookie.setDomain(host)
        jsessionCookie.setPath('/')
        restClient.getClient().getCookieStore().addCookie(jsessionCookie)
    }
}

def genders = []
def educations = []
def heights = []
def weights = []
def ages = []
def customVariables = []
def folderName = "subject_data"

def subjectListing = restClient.request(GET, JSON) { req ->
    uri.path = "/data/projects/${project}/subjects"
    uri.query = ['format': 'json']
}

for (String subject in subjectListing.ResultSet.Result.label) {
    def subjectInfo = restClient.request(GET) { req ->
        uri.path = "/data/projects/${project}/subjects/${subject}"
        uri.query = ['format': 'json']
    }

    Map demographics = subjectInfo.items.get(0).children.get(0).items.get(0).data_fields
    if (demographics.age) {
        ages << demographics.age
    }
    if (demographics.gender) {
        genders << demographics.gender
    }
    if (demographics.height) {
        heights << demographics.height
    }
    if (demographics.weight) {
        weights << demographics.weight
    }
    if (demographics.education) {
        educations << demographics.education
    }
}

List<Path> pathsToPush = []

pathsToPush.addAll(TeXCompiler.compileBarGraph(ages, 5, "Age Distribution for subjects in: ${project}", "ages"))
pathsToPush.addAll(TeXCompiler.compileBarGraph(heights, 5, "Height Distribution for subjects in: ${project}", "heights"))
pathsToPush.addAll(TeXCompiler.compileBarGraph(weights, 5, "Weight Distribution for subjects in: ${project}", "weights"))
pathsToPush.addAll(TeXCompiler.compileBarGraph(educations, 5, "Education Distribution for subjects in: ${project}", "educations"))

restClient.request(PUT) {
    uri.path = "/data/projects/${project}/resources/${folderName}"
}

for (Path path in pathsToPush) {
    restClient.request(POST) {
        uri.path = "/data/projects/${project}/resources/${folderName}/files"

        requestContentType: 'multipart/form-data'

        MultipartEntity entity = new MultipartEntity()
        entity.addPart('content', new FileBody(path.toFile() as File))
        it.entity = entity
    }
}