const path = require('path');

module.exports = {
    mode: 'development',
    entry: './src/index.ts',
    output: {
        filename: 'main.js',
        path: path.resolve(__dirname, 'dist'),
    },
    devServer: {
        contentBase: './public',
        historyApiFallback: true,
        port: 3000,
        proxy: {
            '/api': {
                target: 'http://localhost:8080/daw',
                pathRewrite: {'^/api': ''},
                changeOrigin: true,
                //onProxyReq(proxyReq, req) {
                //    proxyReq.setHeader('authorization', authorizationHeaderValue)
                //}
            }
        }
    },
    resolve: {
        extensions: ['.tsx', '.ts', '.js'],
    },
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                use: 'ts-loader',
                exclude: /node_modules/,
            },
        ],
    },
};