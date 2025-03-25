const xyValues = new Map()

module.exports = {

    oscInFilter:function(data){
        var {address, args, host, port} = data

        if (address.includes('xy') && !(address.includes('touch'))) {
            var xyAddress = address.slice(0,-5)
            var val = [0,0] // default to 0,0 if key doesn't exist in Map yet

            if(xyValues.has(xyAddress)) {
                var suffix = address.slice(-5)
                var newArg = Math.min(Math.max(args[0].value, 0), 1)
                val = xyValues.get(xyAddress)

                if(suffix.includes('xArg')) { 
                    val[0] = newArg 
                } else if (suffix.includes('yArg')) {
                    val[1] = newArg
                }
            } 

            xyValues.set(xyAddress,val)
            receive(xyAddress, val[0], val[1])
            return // bypass original message
        }

        return {address, args, host, port}
    },

    oscOutFilter:function(data){
        var {address, args, host, port, clientId} = data

        if (address.includes('xy') && !(address.includes('touch'))) {

            xyValues.set(address, [args[0].value, args[1].value])

            send(host, port, address + '_xArg', xyValues.get(address)[0])
            send(host, port, address + '_yArg', xyValues.get(address)[1])
            return // bypass original message 
        } 

        return {address, args, host, port}
    }
}
