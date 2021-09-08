let express = require('express')
let app = express()
let bodyParser = require('body-parser')
let session = require('express-session')

// Moteur de template
app.set('view engine', 'ejs')

//MiddleWare
app.use('/assets', express.static('public'))
app.use(bodyParser.urlencoded({ extended: false}))
app.use(bodyParser.json())

app.use(session({
  secret: 'secretkey',
  resave: false,
  saveUninitialized: true,
  cookie: { secure: false }
}))

app.use(require('./middleware/flash'))

// Routes
app.get('/', (request, response) => {
    console.log(request.session)
    response.render('pages/index', {test: 'Salut'})
})

app.post('/', (request,response) => {
    if (request.body.message ===undefined || request.body.message === ''){

        request.flash('error', "Vous n'avez pas posté de message")
        response.redirect('/')

    }else{

    }
})

app.listen(8080)
console.log("reload2")
